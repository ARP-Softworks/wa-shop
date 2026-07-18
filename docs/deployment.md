# Despliegue — WA Shop

## 1. Objetivo

Publicar **frontend + backend** en una sola imagen Docker. Spring Boot sirve la API REST bajo `/api` y los estáticos de Angular (incluido el fallback a `index.html` para recargas del router). PostgreSQL externo (Neon u otro), medios en Cloudinary. SSL terminado en el reverse proxy / hosting.

## 2. Arquitectura de runtime

```text
Internet (HTTPS)
    │
    ▼
Hosting / reverse proxy (TLS + dominio)
    │
    ▼
Contenedor wa-shop (usuario no root)
  Spring Boot :${SERVER_PORT:-8080}
    ├─ /api/**              REST + Actuator
    ├─ /api/actuator/health Healthcheck
    └─ /**                  Angular SPA (classpath:/static/)
    │
    ├──▶ PostgreSQL (Neon, sslmode=require)
    └──▶ Cloudinary (upload desde backend)
```

## 3. Build de la imagen

Dockerfile multi-stage:

1. **Node 22** — `npm ci` + `ng build --configuration=production` (capa de deps cacheable).
2. **Maven / Temurin 21** — `dependency:go-offline` + `mvn -Pwith-frontend package` (copia `frontend/dist/frontend/browser` → `classpath:/static`).
3. **JRE Alpine** — JAR único, usuario `washop`, healthcheck, logs por stdout.

```bash
# Desde la raíz del repo
docker build -t wa-shop:latest .

# Scripts
./scripts/docker-build.sh wa-shop:latest          # Linux/macOS
pwsh ./scripts/docker-build.ps1 wa-shop:latest    # Windows
```

`.dockerignore` excluye `.env`, secretos, `node_modules`, `target`, `dist`, docs, etc. **Nunca** se copian archivos `.env` a la imagen.

Empaquetado local sin Docker:

```bash
./scripts/build-all.sh
# o
pwsh ./scripts/build-all.ps1
```

## 4. Variables de entorno

Documentar **toda variable nueva** aquí y en el README. No eliminar ni renombrar variables existentes sin justificación explícita. Preferir `--env-file` privado o secretos del hosting.

### Aplicación / runtime

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `SERVER_PORT` | No (default `8080`) | Puerto HTTP interno del contenedor |
| `SPRING_PROFILES_ACTIVE` | Sí en prod | Usar `production` |
| `JAVA_OPTS` | No | Opciones JVM (heap, etc.) |
| `APP_PUBLIC_BASE_URL` | Sí en prod | URL base (medios / fallback) `https://dominio` |
| `PUBLIC_SITE_URL` | Sí en prod | URL canónica SEO (sitemap, robots, OG); HTTPS |
| `GOOGLE_SITE_VERIFICATION` | No | Contenido meta Search Console |
| `BING_SITE_VERIFICATION` | No | Contenido meta Bing |
| `GOOGLE_ANALYTICS_ID` | No | GA4; vacío = no cargar |
| `GOOGLE_TAG_MANAGER_ID` | No | GTM; vacío = no cargar |
| `APP_WHATSAPP_PHONE` | Sí | E.164 |
| `APP_CORS_ALLOWED_ORIGINS` | Solo local | En `production` CORS está deshabilitado (mismo origen) |

### Base de datos (Neon / PostgreSQL)

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `SPRING_DATASOURCE_URL` | Sí | JDBC con SSL |
| `SPRING_DATASOURCE_USERNAME` | Sí | |
| `SPRING_DATASOURCE_PASSWORD` | Sí | |

#### Conexión con Neon

1. Crear proyecto en [Neon](https://neon.tech) y copiar la connection string.
2. Formato JDBC recomendado:

```text
jdbc:postgresql://ep-XXXX.region.aws.neon.tech/neondb?sslmode=require
```

3. Usuario/password del panel Neon → `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`.
4. En redes restringidas, permitir outbound del hosting hacia Neon.
5. Preferir pooler de Neon si el proveedor lo recomienda para serverless / muchos arranques.

Flyway aplica migraciones al arrancar; la app no crea el esquema con `ddl-auto=update`.

### Seguridad / sesión

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `APP_SESSION_TIMEOUT` | No | Default `8h` |
| `APP_COOKIE_SESSION_NAME` | No | Default `WASESSION` (HttpOnly) |
| `APP_COOKIE_SECURE` | Sí en prod | `true` detrás de HTTPS |
| `APP_COOKIE_SAME_SITE` | No | `Strict` recomendado en prod |
| `APP_LOGIN_MAX_FAILED_ATTEMPTS` | No | |
| `APP_LOGIN_LOCK_DURATION_SECONDS` | No | |
| `APP_ADMIN_BOOTSTRAP_EMAIL` | Solo primer deploy | |
| `APP_ADMIN_BOOTSTRAP_PASSWORD` | Solo primer deploy | Rotar y quitar después |

### Pedidos / checkout

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `ORDER_PENDING_EXPIRY_MINUTES` | No | Default `45` — libera stock de `PENDING_PAYMENT` abandonados |
| `ORDER_CHECKOUT_IP_MAX_PER_WINDOW` | No | Default `8` — tope de checkouts por IP |
| `ORDER_CHECKOUT_PHONE_MAX_PER_WINDOW` | No | Default `4` — tope de checkouts por teléfono |
| `ORDER_CHECKOUT_WINDOW_SECONDS` | No | Default `900` (15 min) — ventana del rate limit |
| `PAYMENT_PROVIDER` | No | Default `mercadopago` |
| `MERCADOPAGO_ACCESS_TOKEN` | Sí si checkout activo | Solo servidor |
| `MERCADOPAGO_PUBLIC_KEY` | Sí si checkout activo | Pública (también puede exponerse en settings) |
| `MERCADOPAGO_WEBHOOK_SECRET` | Sí si checkout activo | Verificación HMAC del webhook |

Autenticación: cookie de sesión + CSRF (`XSRF-TOKEN` / `X-XSRF-TOKEN`). No JWT en localStorage. Perfil `production` usa `server.forward-headers-strategy=framework` para respetar HTTPS del proxy.

### Medios (Cloudinary)

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `MEDIA_PROVIDER` | Sí en prod | `cloudinary` |
| `MEDIA_MAX_FILE_SIZE` | No | Bytes (default `5242880`) |
| `MEDIA_ALLOWED_TYPES` | No | MIME permitidos |
| `CLOUDINARY_CLOUD_NAME` | Si cloudinary | |
| `CLOUDINARY_API_KEY` | Si cloudinary | **Solo servidor** |
| `CLOUDINARY_API_SECRET` | Si cloudinary | **Solo servidor** |

#### Configuración de Cloudinary

1. Crear cuenta / cloud en Cloudinary.
2. Copiar Cloud name, API Key y API Secret al entorno del contenedor (nunca al frontend).
3. `MEDIA_PROVIDER=cloudinary`.
4. Subidas: `POST /api/admin/media/upload` (admin autenticado). PostgreSQL solo guarda URL + metadatos.
5. Costos y cuotas: ver plan comercial de Cloudinary; no están hardcodeados en la app.

`MEDIA_PROVIDER=local` es solo para desarrollo (`MEDIA_LOCAL_DIR`).

## 5. Dominio y SSL

1. Apuntar el dominio (A/CNAME) al hosting / load balancer.
2. Terminar TLS en el proveedor (Let's Encrypt, Cloudflare, etc.). La app **no** gestiona certificados.
3. Proxy → contenedor en `SERVER_PORT` (HTTP interno).
4. Configurar `APP_PUBLIC_BASE_URL=https://tu-dominio`.
5. `APP_COOKIE_SECURE=true` y SameSite adecuado.
6. Verificar redirección HTTP→HTTPS en el edge.

## 6. Migraciones al arrancar

- Flyway corre en el startup (`classpath:db/migration`, `V1`…`V8`, `V901`).
- Perfil `local` también carga `classpath:db/dev` (`V900` seed demo). **Producción no debe usar seed.**
- Antes de un deploy con migraciones nuevas: backup de Neon.
- Nunca editar migraciones ya aplicadas en entornos compartidos; crear `V(n+1)__...sql`.
- Si Flyway falla, el contenedor no queda “healthy”: revisar logs stdout y revertir imagen (rollback).

## 7. Healthchecks

| Endpoint | Auth | Uso |
|----------|------|-----|
| `GET /api/health` | Público | Health simple `{ "status": "UP" }` |
| `GET /api/actuator/health` | Público | Actuator (base-path `/api/actuator`) |

El `HEALTHCHECK` del Dockerfile y de Compose consulta Actuator. Script:

```bash
./scripts/verify-health.sh http://127.0.0.1:8080
pwsh ./scripts/verify-health.ps1 http://127.0.0.1:8080
```

También comprueba que `/api/admin/**` no sea público y que una ruta SPA responda HTML.

## 8. SPA routing (recarga del navegador)

`SpaWebConfig`:

1. Controllers `/api/**` tienen prioridad.
2. Archivos existentes en `classpath:/static/` se sirven tal cual.
3. Rutas desconocidas **no API** → `index.html` (Angular Router).

Verificado por tests de integración (`SpaRoutingAndSecurityIntegrationTest`) y por `verify-health` contra `/catalogo/...`.

## 9. Seguridad administrativa

- `/api/admin/**` exige rol `ADMIN` (sesión autenticada).
- Sin cookie de sesión → `401`.
- Tests: `AdminPanelIntegrationTest`, `AuthenticationIntegrationTest`, `SpaRoutingAndSecurityIntegrationTest`.

## 10. Primer administrador

1. Definir en el **primer** arranque (solo si el email no existe):

```text
APP_ADMIN_BOOTSTRAP_EMAIL=admin@tu-dominio
APP_ADMIN_BOOTSTRAP_PASSWORD=<password-fuerte-temporal>
```

2. Arrancar el contenedor; `AdminBootstrapRunner` crea el usuario.
3. Ingresar en `/admin/login`.
4. **Quitar** las variables de bootstrap del entorno y rotar la contraseña.
5. No reutilizar el seed local (`admin@washop.uy`) en producción.

## 11. Backups

- Habilitar backups automáticos en Neon (PITR / snapshots según plan).
- Antes de migraciones mayores: snapshot manual o `pg_dump` lógico.
- Guardar dumps cifrados fuera del proveedor de app (object storage).
- Probar restauración en un entorno staging al menos una vez.
- Las imágenes viven en Cloudinary: no entran en el backup de PostgreSQL (solo URLs/metadatos).

## 12. Rollback

1. Mantener la imagen anterior etiquetada (`wa-shop:prev`, digest del registry).
2. Si el deploy falla (healthcheck / Flyway):
   - Revertir el servicio a la imagen anterior.
   - Si la migración ya corrió y no es compatible hacia atrás: restaurar backup de Neon **antes** de reintentar, o aplicar una migración correctiva `V(n+1)`.
3. No hacer `flyway repair` a ciegas en producción.
4. Documentar el digest desplegado en cada release.

## 13. Ejecución local con Docker Compose

Solo Postgres:

```bash
docker compose up -d db
# Si 5432 está ocupado en el host:
# POSTGRES_HOST_PORT=5433 docker compose up -d db
```

App + DB (perfil `full`):

```bash
docker compose --profile full up --build
```

Ejemplo de producción (referencia, secretos por entorno):

```bash
cp docker-compose.production.example.yml docker-compose.production.yml
# completar variables / secretos privados
docker compose -f docker-compose.production.yml up -d --build
```

## 14. Logs

- La JVM escribe a **stdout/stderr** (capturados por Docker / plataforma).
- Perfil `production`: root `WARN`, `uy.washop` `INFO`, patrón ISO8601 en consola.
- No loguear passwords, access tokens ni secretos de Cloudinary.

## 15. Checklist de publicación

- [ ] Variables de entorno configuradas (sin secretos en git)
- [ ] Neon con `sslmode=require` y backup activo
- [ ] Cloudinary verificado (upload de prueba como admin)
- [ ] Dominio + HTTPS en el proxy
- [ ] `SPRING_PROFILES_ACTIVE=production`
- [ ] Admin bootstrap creado, vars removidas, password rotado
- [ ] Healthcheck verde (`/api/actuator/health`)
- [ ] Recarga de `/catalogo/...` y `/admin/...` sirve la SPA
- [ ] `/api/admin/**` responde 401 sin sesión
- [ ] Catálogo público y login admin smoke-tested

## 16. Limitaciones de infraestructura v1

- Un solo contenedor de aplicación (monolito).
- SSL no gestionado por la app.
- Mercado Pago no desplegado.
- Sin CDN obligatorio para la SPA (Cloudinary cubre imágenes de producto).
