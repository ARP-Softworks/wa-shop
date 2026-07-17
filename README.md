# WA Shop

Sistema web para **WA Shop**, empresa uruguaya dedicada a la venta de iPhone nuevos y usados, accesorios y servicios técnicos.

Los clientes exploran el catálogo público y consultan por WhatsApp. Los administradores gestionan productos, categorías, accesorios, servicios técnicos, consultas, imágenes y configuración del sitio desde un panel autenticado.

## Alcance de la primera versión

**Incluye**

- Sitio público responsive (catálogo iPhone / accesorios, filtros, detalle, servicio técnico, consulta WhatsApp).
- Panel administrativo con login.
- CRUD de productos, categorías, accesorios y servicios técnicos.
- Gestión de consultas e imágenes.
- Configuración general del sitio.
- Publicar / despublicar productos.
- Control básico de stock.
- Auditoría básica de modificaciones administrativas.

**No incluye (v1)**

- Carrito de compra, checkout ni pasarela obligatoria.
- Envíos automatizados.
- Facturación ni integración contable.
- Microservicios.
- App móvil.
- Integración obligatoria con Mercado Pago (queda como módulo opcional de etapa 2).

## Tecnologías

| Área | Stack |
|------|--------|
| Frontend | Angular 19 + TypeScript + SCSS (standalone, Reactive Forms, Router, HttpClient, interceptors, Signals) |
| Backend | Java 21 + Spring Boot 3.4 + Spring Web + Spring Data JPA + Spring Security + Bean Validation + Maven |
| DB | PostgreSQL (Neon-compatible) + Flyway |
| Medios | Cloudinary (prod) o almacenamiento local (dev); solo URLs/metadatos en PostgreSQL |
| Deploy | Una imagen Docker; SPA Angular embebida y servida por Spring Boot |

## Requisitos locales

- JDK 21+
- Maven 3.9+
- Node.js 20+ y npm (solo para desarrollo/build del frontend)
- Angular CLI 19 (opcional; los scripts usan el CLI del proyecto)
- PostgreSQL 15+ (local) o instancia Neon
- Docker (opcional, para build/ejecución integrada)

## Configuración mediante variables de entorno

Copiar `.env.example` a `.env` y completar valores. **No commitear secretos.**

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto HTTP de Spring Boot | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil: `local`, `test`, `production` (obligatorio; sin default) | — |
| `SPRING_DATASOURCE_URL` | JDBC PostgreSQL (Neon-compatible) | `jdbc:postgresql://localhost:5432/washop` |
| `SPRING_DATASOURCE_USERNAME` | Usuario DB | `washop` |
| `SPRING_DATASOURCE_PASSWORD` | Password DB | `*****` |
| `APP_SESSION_TIMEOUT` | Expiración de sesión (Duration) | `8h` |
| `APP_COOKIE_SESSION_NAME` | Nombre cookie de sesión HttpOnly | `WASESSION` |
| `APP_COOKIE_SECURE` | Cookie Secure (`true` en producción HTTPS) | `false` / `true` |
| `APP_COOKIE_SAME_SITE` | SameSite de cookies (`Lax` local, `Strict` prod) | `Lax` |
| `APP_LOGIN_MAX_FAILED_ATTEMPTS` | Intentos fallidos antes de bloqueo temporal | `5` |
| `APP_LOGIN_LOCK_DURATION_SECONDS` | Duración del bloqueo (segundos) | `900` |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos (solo perfil `local`) | `http://localhost:4200` |
| `APP_WHATSAPP_PHONE` | Teléfono WhatsApp (E.164) | `5989XXXXXXX` |
| `APP_PUBLIC_BASE_URL` | URL base (medios / fallback) | `http://localhost:8080` |
| `PUBLIC_SITE_URL` | URL canónica SEO (sitemap, OG, canonical); HTTPS en prod | igual que base o vacío → fallback |
| `GOOGLE_SITE_VERIFICATION` | Meta Google Search Console (opcional) | vacío |
| `BING_SITE_VERIFICATION` | Meta Bing Webmaster (opcional) | vacío |
| `GOOGLE_ANALYTICS_ID` | GA4 (opcional; no carga si vacío) | vacío |
| `GOOGLE_TAG_MANAGER_ID` | GTM (opcional; no carga si vacío) | vacío |
| `MEDIA_PROVIDER` | Proveedor de medios: `local` (solo desarrollo) o `cloudinary` | `local` |
| `APP_MEDIA_PROVIDER` | Alias legacy de `MEDIA_PROVIDER` | |
| `MEDIA_MAX_FILE_SIZE` | Tamaño máximo de archivo en bytes | `5242880` (5 MiB) |
| `MEDIA_ALLOWED_TYPES` | MIME permitidos (separados por coma) | `image/jpeg,image/png,image/webp` |
| `MEDIA_LOCAL_DIR` | Directorio local si `MEDIA_PROVIDER=local` | `./data/media` |
| `CLOUDINARY_CLOUD_NAME` | Cloud name (solo backend) | |
| `CLOUDINARY_API_KEY` | API key (solo backend; no exponer al frontend) | |
| `CLOUDINARY_API_SECRET` | API secret (solo backend; no exponer al frontend) | |
| `APP_ADMIN_BOOTSTRAP_EMAIL` | Admin inicial (solo si no existe) | |
| `APP_ADMIN_BOOTSTRAP_PASSWORD` | Password admin inicial (solo bootstrap) | |

Cada variable nueva **debe** documentarse aquí y en `docs/deployment.md`.

## Base de datos local (Docker)

Solo PostgreSQL:

```bash
docker compose up -d db
```

Credenciales por defecto del compose: usuario/password/db `washop`.

App completa (imagen monolito + Postgres):

```bash
docker compose --profile full up --build
```

## Ejecución del backend

```bash
cd backend
# Perfil local obligatorio (seed demo + CORS hacia Angular). Nunca omitir en desarrollo.
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Sin `SPRING_PROFILES_ACTIVE` / perfil explícito la app no carga `db/dev` ni CORS de desarrollo. En producción usar `production`.

API base: `http://localhost:8080/api`  
Health: `http://localhost:8080/api/health`  
Actuator health: `http://localhost:8080/api/actuator/health`

### Autenticación administrativa

- Sesión en cookie HttpOnly (`WASESSION`), Secure en producción, SameSite configurable.
- CSRF con cookie `XSRF-TOKEN` (legible) + header `X-XSRF-TOKEN` (compatible con Angular).
- Endpoints:
  - `GET /api/auth/csrf`
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- Público: `/api/public/**` (sin auth). Admin: `/api/admin/**` (rol `ADMIN`).
- No se usan tokens en `localStorage`.

### Medios / imágenes

- Subida administrativa: `POST /api/admin/media/upload` (el frontend nunca recibe secretos del proveedor).
- En perfil `local`, `MEDIA_PROVIDER=local` guarda archivos en `MEDIA_LOCAL_DIR` y los sirve en `/media/**`.
- En producción usar `MEDIA_PROVIDER=cloudinary` con `CLOUDINARY_*`.
- Tipos: JPEG, PNG, WebP (validación por contenido). Tamaño: `MEDIA_MAX_FILE_SIZE`.
- Costos/límites comerciales de Cloudinary: ver documentación del proveedor; no están hardcodeados en la app.

## Ejecución del frontend

```bash
cd frontend
npm install
npm start
```

App: `http://localhost:4200` (proxy de `/api` hacia `http://localhost:8080`).

Rutas públicas SEO:

- `/` inicio · `/iphone` · `/iphone/nuevos` · `/iphone/usados` · `/iphone/{slug}`
- `/accesorios` · `/accesorios/categoria/{slug}` · `/accesorios/{slug}`
- `/servicio-tecnico` · `/servicio-tecnico/{slug}` · `/contacto` · `/preguntas-frecuentes`
- `/robots.txt` · `/sitemap.xml` (servidos por Spring Boot)

Admin (requiere sesión): `/admin` · Login: `/admin/login`

Documentación SEO: `docs/seo-audit.md`, `docs/seo-strategy.md`, `docs/seo-url-strategy.md`.

### SEO local y Search Console (checklist del propietario)

1. Completar en **Admin → Configuración**: nombre, WhatsApp, dirección, ciudad/departamento/país, horarios, URL pública (`https://…`), imagen social.
2. Crear/verificar [Google Business Profile](https://business.google.com) con **el mismo** nombre, dirección y teléfono que el sitio.
3. En [Google Search Console](https://search.google.com/search-console): verificar dominio (meta `GOOGLE_SITE_VERIFICATION` o DNS) y **enviar** `https://tu-dominio/sitemap.xml`.
4. En [Bing Webmaster Tools](https://www.bing.com/webmasters): verificar y enviar el mismo sitemap.
5. No inventar sucursales ni ciudades donde no operen.

## Compilación integrada

```bash
# Windows
./scripts/build-all.ps1

# Linux / macOS
./scripts/build-all.sh
```

Pasos equivalentes a mano:

```bash
cd frontend
npm ci
npm run build -- --configuration=production

cd ../backend
mvn -B -Pwith-frontend clean package
```

El perfil Maven `with-frontend` copia `frontend/dist/frontend/browser` al classpath estático del JAR.

Para empaquetar el backend **sin** frontend (tests / CI parcial):

```bash
cd backend
mvn -B clean package
```

## Ejecución con Docker

```bash
docker build -t wa-shop:local .
docker run --rm -p 8080:8080 --env-file .env wa-shop:local
```

Scripts:

| Script | Uso |
|--------|-----|
| `scripts/build-all.(ps1\|sh)` | Tests + build Angular prod + package Spring con estáticos |
| `scripts/docker-build.(ps1\|sh)` | `docker build` multi-stage |
| `scripts/verify-health.(ps1\|sh)` | Health, admin no público, smoke SPA |

La imagen única contiene el JAR de Spring Boot con los estáticos de Angular. Spring Boot sirve la SPA y reenvía rutas no-API a `index.html`. Guía: `docs/deployment.md`.

## Migraciones de base de datos

- Flyway aplica migraciones al iniciar (`backend/src/main/resources/db/migration`).
- Migraciones: `V1`…`V8` + `V901` (schema) y `db/dev/V900__dev_seed.sql` solo con perfil `local`.
- No alterar migraciones ya aplicadas; crear una nueva versión.
- Detalle del modelo: `docs/database-model.md`.

## Datos de prueba

- Seed de desarrollo: `backend/src/main/resources/db/dev/V900__dev_seed.sql` (solo perfil `local`).
- Admin demo: `admin@washop.uy` / `ChangeMe123!` (hash BCrypt; no usar en producción).
- Incluye categorías, iPhones nuevo/usado, accesorio, imágenes, features, servicio técnico, settings y una consulta.
- Los seeds **no** deben usarse en producción.

## Pruebas

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

Antes de cerrar una tarea: compilar y ejecutar las pruebas relevantes.

## Estructura del proyecto

```text
wa-shop/
├── AGENTS.md
├── README.md
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── docker-compose.production.example.yml
├── .env.example
├── docs/
│   └── deployment.md
├── scripts/
│   ├── build-all.ps1 / build-all.sh
│   ├── docker-build.ps1 / docker-build.sh
│   └── verify-health.ps1 / verify-health.sh
├── frontend/                   # Angular 19
└── backend/                    # Spring Boot (Maven)
```

Detalle de carpetas: ver `docs/architecture.md`.

## Publicación

1. Configurar PostgreSQL (Neon u otro) y variables de entorno.
2. Configurar proveedor de imágenes.
3. Construir imagen Docker.
4. Desplegar en el hosting elegido (SSL gestionado por el proveedor).
5. Verificar healthcheck, login admin y catálogo público.

Guía completa: `docs/deployment.md`.

## Limitaciones actuales

- Sin carrito ni pagos online en v1.
- Consulta comercial vía WhatsApp (no checkout in-app).
- El mockup en `docs/mockups/wa-shop-mockup.png` es la fuente de verdad visual.

## Funcionalidades futuras

- Módulo opcional Mercado Pago (sin reescribir el catálogo).
- Carrito y checkout.
- Envíos / logística.
- Facturación y contabilidad.
- Roles administrativos adicionales.
- App móvil.

## Documentación

| Documento | Contenido |
|-----------|-----------|
| [AGENTS.md](./AGENTS.md) | Reglas para agentes/desarrolladores |
| [docs/architecture.md](./docs/architecture.md) | Arquitectura y estructura de carpetas |
| [docs/functional-requirements.md](./docs/functional-requirements.md) | Requisitos funcionales |
| [docs/database-model.md](./docs/database-model.md) | Modelo de datos |
| [docs/api-contract.md](./docs/api-contract.md) | Contrato REST |
| [docs/deployment.md](./docs/deployment.md) | Despliegue |
| [docs/implementation-plan.md](./docs/implementation-plan.md) | Plan de implementación |
