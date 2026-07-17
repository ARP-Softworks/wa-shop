# Contrato API — WA Shop

Base URL: `/api`  
Formato: JSON (`application/json`)  
Errores: documento de error uniforme (sección 8).

> Contrato **propuesto** para v1. Puede ajustarse tras confirmar decisiones D1–D12.

## 1. Convenciones

| Tema | Convención |
|------|------------|
| Auth admin | Cookie de sesión HttpOnly `WASESSION` + CSRF (`XSRF-TOKEN` / header `X-XSRF-TOKEN`) |
| Paginación | `page` (0-based), `size`, respuesta `{ content, page, size, totalElements, totalPages }` |
| Filtros | query params |
| Fechas | ISO-8601 UTC |
| Dinero | `number` decimal + `currency` ISO-4217 |
| IDs | UUID string |

## 2. Auth

Sesión servidor (no JWT). El frontend Angular usa `withCredentials` y el interceptor CSRF.

### `GET /api/auth/csrf`

Response `200`:

```json
{
  "token": "...",
  "headerName": "X-XSRF-TOKEN",
  "parameterName": "_csrf"
}
```

También establece cookie `XSRF-TOKEN` (legible por JS).

### `POST /api/auth/login`

Requiere CSRF. Request:

```json
{ "email": "admin@washop.uy", "password": "********" }
```

Response `200` (establece cookie `WASESSION`):

```json
{
  "id": "...",
  "email": "admin@washop.uy",
  "firstName": "...",
  "lastName": "...",
  "role": "ADMIN"
}
```

Errores: `401` credenciales inválidas; `429` lockout por intentos fallidos.

### `POST /api/auth/logout`

Requiere CSRF + sesión. Response: `204`. Invalida la sesión.

### `GET /api/auth/me`

Requiere sesión autenticada. Response: mismo shape que login.

## 3. Catálogo público — productos (iPhone)

### `GET /api/products`

Query: `condition`, `model`, `storageCapacity`, `color`, `minPrice`, `maxPrice`, `featured`, `categoryId`, `q`, `page`, `size`, `sort`.

Response item (sin IMEI):

```json
{
  "id": "...",
  "name": "iPhone 14",
  "model": "A2882",
  "slug": "iphone-14-128-midnight",
  "condition": "USED",
  "storageCapacity": "128GB",
  "color": "Midnight",
  "batteryHealth": 92,
  "priceCurrent": 18990.00,
  "pricePrevious": 20990.00,
  "currency": "UYU",
  "stock": 1,
  "warranty": "3 meses",
  "featured": true,
  "primaryImageUrl": "https://...",
  "category": { "id": "...", "name": "iPhone 14", "slug": "iphone-14" }
}
```

Solo `published=true`.

### `GET /api/products/{slugOrId}`

Detalle público + `images[]`, `features`, `description`. **Sin IMEI.**

## 4. Accesorios públicos

### `GET /api/accessories`

### `GET /api/accessories/{slugOrId}`

Misma idea que productos; campos propios de accesorio.

## 5. Servicios técnicos públicos

### `GET /api/technical-services`

Lista publicados ordenados.

### `GET /api/technical-services/{slugOrId}`

Detalle.

## 6. Site config pública

### `GET /api/site-settings/public`

Subconjunto no sensible: nombre, WhatsApp, textos de UI, moneda default.

## 7. Consultas (públicas)

### `POST /api/inquiries`

```json
{
  "source": "WHATSAPP_CLICK",
  "customerName": "opcional",
  "customerPhone": "opcional",
  "message": "Consulta iPhone 14...",
  "productId": "...",
  "accessoryId": null,
  "serviceId": null
}
```

Response `201` con `id` y `whatsappUrl` sugerida (o el cliente la arma).

## 8. Admin — productos

Prefijo: `/api/admin/products` (sesión admin + CSRF + rol ADMIN).

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/admin/products` | Listado (incluye no publicados e IMEI) |
| GET | `/api/admin/products/{id}` | Detalle admin |
| POST | `/api/admin/products` | Crear |
| PUT | `/api/admin/products/{id}` | Actualizar |
| DELETE | `/api/admin/products/{id}` | Eliminar |
| PATCH | `/api/admin/products/{id}/publish` | `{ "published": true }` |
| PATCH | `/api/admin/products/{id}/stock` | `{ "stock": 2 }` |
| PATCH | `/api/admin/products/{id}/featured` | `{ "featured": true }` |

Body create/update (ejemplo):

```json
{
  "name": "iPhone 13",
  "model": "A2633",
  "description": "...",
  "condition": "NEW",
  "storageCapacity": "256GB",
  "color": "Blue",
  "batteryHealth": null,
  "priceCurrent": 25990,
  "pricePrevious": null,
  "currency": "UYU",
  "stock": 3,
  "warranty": "12 meses",
  "imei": "490154203237518",
  "features": ["Face ID", "Ceramic Shield"],
  "categoryId": "...",
  "published": false,
  "featured": false
}
```

## 9. Admin — categorías / accesorios / servicios

| Recurso | Base path |
|---------|-----------|
| Categorías | `/api/admin/categories` |
| Accesorios | `/api/admin/accessories` |
| Servicios técnicos | `/api/admin/technical-services` |

CRUD REST estándar (`GET` lista/detalle, `POST`, `PUT`, `DELETE`) + publish donde aplique.

## 10. Admin — imágenes

### `POST /api/admin/media/upload`

`multipart/form-data`:

| Parte / param | Descripción |
|---------------|-------------|
| `file` | Archivo (JPEG, PNG o WebP; validado por magic bytes) |
| `folder` | Carpeta lógica opcional (`products`, `logos`, …) |

Response `201`:

```json
{
  "url": "https://...",
  "publicId": "wa-shop/products/abc",
  "provider": "cloudinary",
  "format": "jpg",
  "contentType": "image/jpeg",
  "sizeBytes": 245760,
  "width": 1200,
  "height": 1200
}
```

### `DELETE /api/admin/media`

Body: `{ "publicId": "..." }`.

Responde `409` si la imagen sigue referenciada en productos o en el logo de configuración.

### Asociación

Las URLs/`publicId` se asocian al guardar productos (`images[]` en write DTO) o settings (`logoUrl` / `logoPublicId`). Alt text, orden (`position`) e imagen principal (`mainImage`) se gestionan en el formulario admin.

## 11. Admin — consultas y settings

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/admin/inquiries` | Filtro `status` |
| GET | `/api/admin/inquiries/{id}` | Detalle |
| PATCH | `/api/admin/inquiries/{id}` | `{ status, adminNotes }` |
| GET | `/api/admin/site-settings` | Todas las keys |
| PUT | `/api/admin/site-settings` | Upsert mapa key→value |

## 12. Admin — auditoría

### `GET /api/admin/audit-logs`

Query: `entityType`, `entityId`, `actorAdminId`, `from`, `to`, `page`, `size`.

## 13. Health

### `GET /api/health`

```json
{ "status": "UP" }
```

## 14. Errores

```json
{
  "timestamp": "2026-07-15T22:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "pricePrevious must be greater than priceCurrent",
  "path": "/api/admin/products",
  "details": [
    { "field": "pricePrevious", "message": "must be greater than priceCurrent" }
  ]
}
```

| HTTP | Uso |
|------|-----|
| 400 | Validación |
| 401 | No autenticado |
| 403 | Sin permiso / IMEI u otros recursos |
| 404 | No encontrado |
| 409 | Conflicto (slug/IMEI duplicado) |
| 500 | Error interno (sin stack trace) |

## 15. Módulo pagos (futuro, no v1)

Reservar namespace `/api/payments/**` o `/api/admin/payments/**` sin implementar. El catálogo no depende de estas rutas.
