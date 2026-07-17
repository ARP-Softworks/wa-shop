# Modelo de base de datos — WA Shop

PostgreSQL + Flyway. Compatible con Neon. **Sin binarios de imagen en la base.**

## 1. Decisiones de diseño (v1 implementada)

| Requisito | Solución |
|-----------|----------|
| iPhone nuevos | `products.product_type = IPHONE` + `condition = NEW` |
| iPhone usados | `IPHONE` + `USED` + `battery_health` opcional (0–100) |
| Accesorios | `product_type = ACCESSORY` (misma tabla `products`) |
| Servicios técnicos | Tabla `technical_services` |
| Galería de imágenes | `product_images` con URL/`public_id` (sin BYTEA/BLOB) |
| Variantes y atributos | Columnas tipadas + `product_features` (name/value) |
| Stock / publicación | `stock >= 0`, `published`, `featured` |
| Consultas | `inquiries` con `product_id` opcional |
| Configuración | Fila única en `site_settings` |
| Administradores | `users` con `role = ADMIN`, password BCrypt |
| Auditoría | `audit_logs` (sin cascada peligrosa; FK usuario `ON DELETE SET NULL`) |
| Dinero | `NUMERIC(12,2)` / `BigDecimal` — **nunca `double`** |
| IMEI | Opcional, único si existe; **solo en DTO admin** |
| Salud de batería | Solo válida para iPhone usado (`ProductRules`) |
| Enums | Persistidos como `VARCHAR` / `EnumType.STRING` |
| IDs | UUID en todas las tablas |
| Relaciones | Unidireccionales preferidas; cascada solo imágenes/features → producto |

**Nota sobre accesorios:** se unificaron en `products` (vía `product_type`) en lugar de una tabla `accessories` separada, para compartir galería, features, stock y publicación. El contrato API puede seguir exponiendo rutas `/accessories` filtrando por tipo.

## 2. Diagrama lógico

```text
users ──────────────────────────── audit_logs
                                   (user_id SET NULL)

categories ◄──── products ◄──── product_images
                  │   │
                  │   └── product_features
                  │
                  └── inquiries (product_id opcional, SET NULL)

technical_services
site_settings (singleton lógico)
```

## 3. Tablas

### 3.1 `users`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| email | VARCHAR(320) UNIQUE NOT NULL | |
| password_hash | VARCHAR(100) NOT NULL | BCrypt |
| first_name | VARCHAR(100) NOT NULL | |
| last_name | VARCHAR(100) NOT NULL | |
| role | VARCHAR(40) NOT NULL | `ADMIN` |
| enabled | BOOLEAN NOT NULL DEFAULT TRUE | |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

Índice: `idx_users_enabled`.

### 3.2 `categories`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| name | VARCHAR(120) NOT NULL | |
| slug | VARCHAR(140) UNIQUE NOT NULL | |
| description | TEXT | |
| active | BOOLEAN NOT NULL DEFAULT TRUE | |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

### 3.3 `products`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| slug | VARCHAR(220) UNIQUE NOT NULL | |
| name | VARCHAR(200) NOT NULL | |
| model | VARCHAR(120) | |
| description | TEXT | |
| product_type | VARCHAR(40) NOT NULL | `IPHONE`, `ACCESSORY` |
| condition | VARCHAR(20) NOT NULL | `NEW`, `USED` |
| storage_capacity | VARCHAR(40) | |
| color | VARCHAR(80) | |
| battery_health | SMALLINT | 0–100; solo iPhone usado (app) |
| price | NUMERIC(12,2) NOT NULL | |
| previous_price | NUMERIC(12,2) | debe ser > price (app) |
| currency | CHAR(3) NOT NULL | `UYU`, `USD` |
| stock | INT NOT NULL DEFAULT 0 | CHECK >= 0 |
| warranty | VARCHAR(200) | |
| imei | VARCHAR(20) UNIQUE | nullable; longitud 14–17 |
| published | BOOLEAN NOT NULL DEFAULT FALSE | |
| featured | BOOLEAN NOT NULL DEFAULT FALSE | |
| category_id | UUID FK → categories | `ON DELETE SET NULL` |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

Índices: `(published, featured)`, `(product_type, condition)`, `category_id`, `price`, `model`.

### 3.4 `product_images`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| product_id | UUID FK NOT NULL | `ON DELETE CASCADE` |
| url | TEXT NOT NULL | CDN HTTPS |
| public_id | VARCHAR(255) | Cloudinary/S3 key |
| alt_text | VARCHAR(255) | |
| position | INT NOT NULL DEFAULT 0 | |
| main_image | BOOLEAN NOT NULL DEFAULT FALSE | unicidad en aplicación |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

### 3.5 `product_features`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| product_id | UUID FK NOT NULL | `ON DELETE CASCADE` |
| name | VARCHAR(120) NOT NULL | |
| feature_value | VARCHAR(500) NOT NULL | Java: `value` |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

UNIQUE `(product_id, name)`.

### 3.6 `technical_services`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| name | VARCHAR(200) NOT NULL | |
| slug | VARCHAR(220) UNIQUE NOT NULL | |
| description | TEXT | |
| price | NUMERIC(12,2) | orientativo |
| currency | CHAR(3) | `UYU` / `USD` |
| estimated_time | VARCHAR(120) | |
| active | BOOLEAN NOT NULL DEFAULT TRUE | |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

### 3.7 `inquiries`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| customer_name | VARCHAR(200) NOT NULL | |
| phone | VARCHAR(40) | |
| email | VARCHAR(320) | |
| message | TEXT NOT NULL | |
| product_id | UUID FK | `ON DELETE SET NULL` |
| status | VARCHAR(40) NOT NULL | `NEW`, `IN_PROGRESS`, `CLOSED` |
| source | VARCHAR(40) NOT NULL | `WHATSAPP_CLICK`, `FORM`, `OTHER` |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

### 3.8 `site_settings`

Fila única de configuración (ID canónico en seed / `SiteSettings.DEFAULT_ID`).

| Columna | Tipo |
|---------|------|
| id | UUID PK |
| business_name | VARCHAR(200) NOT NULL |
| whatsapp_number | VARCHAR(40) |
| instagram_url | VARCHAR(500) |
| address | VARCHAR(500) |
| opening_hours | VARCHAR(500) |
| contact_email | VARCHAR(320) |
| logo_url | TEXT |
| created_at / updated_at | TIMESTAMPTZ NOT NULL |

### 3.9 `audit_logs`

| Columna | Tipo | Notas |
|---------|------|-------|
| id | UUID PK | |
| user_id | UUID FK | `ON DELETE SET NULL` |
| action | VARCHAR(40) NOT NULL | `CREATE`, `UPDATE`, `DELETE`, `PUBLISH`, `UNPUBLISH`, `LOGIN`, `OTHER` |
| entity_type | VARCHAR(80) NOT NULL | |
| entity_id | VARCHAR(64) | |
| details | TEXT | sin secretos |
| created_at / updated_at | TIMESTAMPTZ NOT NULL | |

## 4. Migraciones Flyway

| Versión | Contenido |
|---------|-----------|
| `V1__users_and_categories.sql` | users, categories |
| `V2__products_images_features.sql` | products, product_images, product_features |
| `V3__technical_services.sql` | technical_services |
| `V4__inquiries.sql` | inquiries |
| `V5__site_settings.sql` | site_settings |
| `V6__audit_logs.sql` | audit_logs |
| `db/dev/V900__dev_seed.sql` | seed **solo perfil `local`** |

Ubicaciones:

- Producción / test: `classpath:db/migration`
- Local: `classpath:db/migration,classpath:db/dev`

### Seed de desarrollo (local)

- Admin: `admin@washop.uy` / `ChangeMe123!` (BCrypt)
- Categorías iPhone y Accesorios
- 1 iPhone nuevo, 1 usado (con IMEI), 1 accesorio
- Imágenes, features, servicio técnico, settings e inquiry de ejemplo

**No usar el seed en producción.**

## 5. Capas Java

Paquete raíz: `uy.washop`.

Por módulo: `domain` (entidades/enums/reglas), `infrastructure` (repositorios JPA), `api.dto` + `api.mapper` (proyecciones).

- Público: `ProductPublicResponse` **sin IMEI**
- Admin: `ProductAdminResponse` **con IMEI**
- Reglas: `ProductRules` (batería, precio anterior), `ProductImageRules` (una main)

## 6. Proyecciones de seguridad

- DTOs públicos excluyen `imei` y `password_hash`.
- Validación Bean Validation en entidades.
- Constraints de negocio reforzados en DB + aplicación.
