# Arquitectura — WA Shop

## 1. Visión

Monolito modular: **un backend Spring Boot** y **un frontend Angular**, empaquetados en **una sola imagen Docker**. Spring Boot expone la API REST bajo `/api` y sirve la SPA Angular en las demás rutas.

```text
                    ┌─────────────────────────────┐
  Browser ─────────▶│  Spring Boot (puerto 8080)  │
                    │  ├─ /api/**  → REST modules │
                    │  └─ /**      → Angular SPA  │
                    └─────────────┬───────────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
        PostgreSQL           Media provider      WhatsApp (deep link)
         (Neon)            (Cloudinary/S3)       (sin backend de chat)
```

## 2. Principios

| Principio | Aplicación |
|-----------|------------|
| Monolito modular | Módulos de dominio en un solo artefacto Maven |
| SPA embebida | Build Angular → recursos estáticos del JAR |
| API first bajo `/api` | Contrato documentado en `api-contract.md` |
| DB solo metadatos de medios | URLs en PostgreSQL; binarios fuera |
| Pagos desacoplados | Puerto/módulo `payments` opcional (Mercado Pago etapa 2) |
| Seguridad por defecto | Solo admin autenticado; catálogo público proyectado |

## 3. Módulos de backend

Paquete raíz propuesto: `uy.washop`.

| Módulo | Responsabilidad |
|--------|-----------------|
| `shared` | Errores, paginación, utilidades, clock, IDs |
| `security` | Spring Security, JWT/sesión, filtros, roles |
| `catalog` | Productos (iPhone), categorías, publicación, stock, destacados |
| `accessories` | Catálogo/CRUD de accesorios (puede reutilizar núcleo de catálogo) |
| `services` | Servicios técnicos (contenido y CRUD) |
| `inquiries` | Consultas de clientes / leads |
| `media` | Upload firmado / proxy a Cloudinary o S3; metadatos |
| `siteconfig` | Configuración general del sitio (WhatsApp, textos, SEO básico) |
| `audit` | Registro de cambios administrativos |
| `payments` *(opcional, stub)* | Interfaces para etapa 2 Mercado Pago; sin implementación obligatoria en v1 |

### Relación catálogo ↔ pagos (etapa 2)

```text
catalog (Product id, price, currency, stock)
        ▲
        │  lee IDs / precios; no conoce MP
payments│  (MercadoPagoPaymentGateway implements PaymentPort)
```

El catálogo emite hechos de negocio (`ProductPublished`, precios). El módulo de pagos consume puertos, no al revés.

## 4. Capas dentro de cada módulo

```text
module/
  api/            Controllers, request/response DTOs, assemblers
  application/    Use cases / services de aplicación
  domain/         Entities, enums, domain services, ports
  infrastructure/ JPA repositories, adapters (media, security hooks)
```

## 5. Frontend Angular

Aplicación única con dos áreas de routing:

| Área | Prefijo | Descripción |
|------|---------|-------------|
| Pública | `/` | Home, catálogo, detalle, servicio técnico, contacto/WhatsApp |
| Admin | `/admin` | Login + CRUDs y gestión |

Organización por features standalone + `core` (interceptors, auth, API) + `shared` (UI ligera).

Estado: Signals para UI state; servicios con `HttpClient` para servidor. Sin NgRx salvo necesidad futura justificada.

## 6. Autenticación y autorización

- V1: rol `ADMIN` únicamente.
- Mecanismo propuesto: **JWT en header `Authorization: Bearer`** (stateless, simple de desplegar). Alternativa a confirmar: cookie httpOnly de sesión.
- Interceptor Angular adjunta el token; interceptor de errores maneja `401/403`.
- Endpoints públicos vs `/api/admin/**`.

## 7. Medios (imágenes)

1. Admin solicita upload (multipart o signed upload).
2. `media` sube al proveedor externo.
3. Se persisten `url`, `public_id`/`key`, `alt`, `sort_order`, `is_primary`.
4. Al borrar producto/imagen se elimina (o marca) el objeto remoto vía adapter.

**Prohibido:** `bytea` / BLOBs de imagen en PostgreSQL.

## 8. Consulta WhatsApp

No hay bot de mensajería en v1. El frontend construye un deep link (`https://wa.me/<phone>?text=...`) con datos del producto y, opcionalmente, registra una `inquiry` en el backend para el panel admin.

## 9. Empaquetado y despliegue

```text
Dockerfile (multi-stage)
  stage node  → npm ci && ng build
  stage maven → copia dist a static/, mvn package
  stage jre   → jar ejecutable
```

SSL: terminación en el proveedor de hosting. La app escucha HTTP interno.

## 10. Estructura de carpetas propuesta

### Raíz

```text
wa-shop/
├── AGENTS.md
├── README.md
├── Dockerfile
├── .dockerignore
├── .gitignore
├── docs/
│   ├── architecture.md
│   ├── functional-requirements.md
│   ├── database-model.md
│   ├── deployment.md
│   ├── implementation-plan.md
│   ├── api-contract.md
│   └── mockups/
│       └── wa-shop-mockup.png
├── scripts/
│   ├── build-all.ps1
│   └── build-all.sh
├── frontend/
└── backend/
```

### Frontend

```text
frontend/
├── angular.json
├── package.json
├── tsconfig*.json
├── proxy.conf.json
└── src/
    ├── index.html
    ├── main.ts
    ├── styles.scss
    ├── environments/
    └── app/
        ├── app.config.ts
        ├── app.routes.ts
        ├── app.component.ts
        ├── core/
        │   ├── auth/
        │   ├── http/
        │   │   ├── auth.interceptor.ts
        │   │   └── error.interceptor.ts
        │   └── api/
        ├── shared/
        │   ├── components/
        │   ├── pipes/
        │   └── models/
        ├── features/
        │   ├── public/
        │   │   ├── home/
        │   │   ├── catalog/
        │   │   ├── product-detail/
        │   │   ├── accessories/
        │   │   ├── technical-service/
        │   │   └── layout/
        │   └── admin/
        │       ├── login/
        │       ├── dashboard/
        │       ├── products/
        │       ├── categories/
        │       ├── accessories/
        │       ├── technical-services/
        │       ├── inquiries/
        │       ├── media/
        │       ├── site-settings/
        │       └── layout/
        └── layouts/
```

### Backend

```text
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/uy/washop/
    │   │   ├── WaShopApplication.java
    │   │   ├── shared/
    │   │   ├── security/
    │   │   ├── catalog/
    │   │   ├── accessories/
    │   │   ├── services/
    │   │   ├── inquiries/
    │   │   ├── media/
    │   │   ├── siteconfig/
    │   │   ├── audit/
    │   │   └── payments/          # stub / ports para etapa 2
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── db/migration/
    │       └── static/            # Angular build (CI/local integrate)
    └── test/
        └── java/uy/washop/
```

## 11. Decisiones abiertas

Ver sección “Decisiones que necesitan confirmación” en la respuesta de diseño / `docs/implementation-plan.md`.

## 12. Referencia visual

Toda decisión de UI (navegación, jerarquía, tipografía, densidad del admin) debe contrastarse con `docs/mockups/wa-shop-mockup.png` **antes** de implementar o alterar pantallas.
