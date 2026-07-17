# Requisitos funcionales — WA Shop

## 1. Contexto

WA Shop vende iPhone nuevos y usados, accesorios y ofrece servicio técnico en Uruguay. La v1 prioriza **descubrimiento de catálogo** + **consulta por WhatsApp** + **backoffice de gestión**.

Referencia de UX/UI: `docs/mockups/wa-shop-mockup.png`.

## 2. Actores

| Actor | Descripción |
|-------|-------------|
| Visitante | Navega el sitio público; no inicia sesión |
| Administrador | Autenticado; gestiona contenido y operaciones |

## 3. Sitio público

### RF-P01 — Navegación pública

- El sitio debe exponer secciones coherentes con el mockup (p. ej. inicio, iPhones, accesorios, servicio técnico, contacto/WhatsApp).
- Navegación usable en móvil, tablet y desktop.

### RF-P02 — Catálogo de iPhone

- Listar productos publicados de tipo iPhone (nuevos y usados).
- Mostrar al menos: imagen principal, nombre/modelo, estado (nuevo/usado), precio actual, precio anterior si existe, moneda, indicador de destacado si aplica.

### RF-P03 — Catálogo de accesorios

- Listar accesorios publicados con imagen, nombre, precio y filtros básicos.

### RF-P04 — Filtros de productos

Filtros mínimos para iPhone:

- Estado: nuevo / usado
- Modelo
- Capacidad
- Color
- Rango de precio
- Disponibilidad (en stock / agotado) — opcional según confirmación
- Destacados

Filtros de accesorios: categoría y precio como mínimo.

### RF-P05 — Detalle de producto

Mostrar:

- Galería e imagen principal
- Nombre, modelo, descripción
- Estado, capacidad, color, salud de batería (si aplica)
- Precio actual / anterior, moneda
- Stock (o “consultar” / “agotado” según reglas de negocio confirmadas)
- Garantía
- Características
- CTA de consulta por WhatsApp
- **No** mostrar IMEI al público

### RF-P06 — Consulta por WhatsApp

- CTA genera enlace a WhatsApp con mensaje prearmado (producto, precio, URL).
- Opcionalmente registra una consulta en el sistema para seguimiento admin.

### RF-P07 — Servicio técnico

- Página informativa con servicios publicados (título, descripción, precio orientativo si existe, condiciones).
- CTA de contacto/WhatsApp.

### RF-P08 — Configuración visible

- Datos de contacto, WhatsApp y textos configurables desde admin (según `site_settings`).

## 4. Panel administrativo

### RF-A01 — Autenticación

- Login de administrador.
- Sesión/token con expiración.
- Logout.
- Rutas `/admin/**` protegidas.

### RF-A02 — CRUD de productos (iPhone)

Campos:

| Campo | Notas |
|-------|-------|
| Nombre | Obligatorio |
| Modelo | Obligatorio |
| Descripción | Texto largo |
| Estado | `NEW` / `USED` |
| Capacidad | p. ej. 128GB |
| Color | |
| Salud de batería | %; tipicamente usados |
| Precio actual | Obligatorio |
| Precio anterior | Opcional |
| Moneda | p. ej. UYU / USD |
| Stock | Entero ≥ 0 |
| Garantía | Texto o meses |
| IMEI | Opcional; **solo admin** |
| Características | Lista/texto estructurado |
| Imagen principal | URL/metadatos |
| Galería | N imágenes |
| Publicado | boolean |
| Destacado | boolean |
| createdAt / updatedAt | Sistema |

Operaciones: crear, editar, eliminar (o archivar), publicar/despublicar, marcar destacado, ajustar stock.

### RF-A03 — CRUD de categorías

- Nombre, slug, tipo (`IPHONE` / `ACCESSORY` / genérico), orden, activo.

### RF-A04 — CRUD de accesorios

- Campos comerciales análogos (nombre, descripción, categoría, precio, moneda, stock, imágenes, publicado, destacado).

### RF-A05 — CRUD de servicios técnicos

- Título, descripción, precio orientativo opcional, publicado, orden.

### RF-A06 — Gestión de consultas

- Listar, filtrar por estado (`NEW`, `IN_PROGRESS`, `CLOSED`), ver detalle, cambiar estado, notas internas.

### RF-A07 — Gestión de imágenes

- Subir, asociar a producto/accesorio/servicio, reordenar, definir principal, eliminar.
- Persistencia remota; DB solo metadatos.

### RF-A08 — Configuración general

- Teléfono WhatsApp, nombre del sitio, textos de home/footer, redes, mensajes por defecto.

### RF-A09 — Auditoría básica

- Registrar quién cambió qué entidad, acción (`CREATE`/`UPDATE`/`DELETE`/`PUBLISH`/…), timestamp y resumen del cambio.

## 5. Reglas de negocio transversales

| ID | Regla |
|----|-------|
| RN-01 | Solo productos/accesorios/servicios **publicados** aparecen en el sitio público |
| RN-02 | Stock 0 ⇒ no se puede “comprar”; sí se puede definir si se permite consulta |
| RN-03 | IMEI nunca en API pública |
| RN-04 | Precio anterior, si existe, debe ser > precio actual (validación) — a confirmar |
| RN-05 | Moneda por ítem; default de sitio configurable |
| RN-06 | Eliminación de producto debe manejar imágenes asociadas (borrar o orphan cleanup) |
| RN-07 | Toda mutación admin genera entrada de auditoría |

## 6. No funcionales (v1)

| ID | Requisito |
|----|-----------|
| RNF-01 | Responsive |
| RNF-02 | API bajo `/api` |
| RNF-03 | Variables de entorno para secretos y config |
| RNF-04 | Una imagen Docker |
| RNF-05 | Compatible con PostgreSQL Neon |
| RNF-06 | Sin microservicios |
| RNF-07 | Compilación + pruebas antes de cerrar tareas |

## 7. Fuera de alcance v1

- Carrito, checkout, Mercado Pago obligatorio
- Envíos, facturación, contabilidad
- App móvil
- Cuentas de clientes finales
- Multi-idioma completo (español Uruguay es el default)

## 8. Criterios de aceptación globales (MVP)

1. Un visitante puede filtrar iPhones y abrir un detalle sin login.
2. Desde el detalle puede abrir WhatsApp con contexto del producto.
3. Un admin puede autenticarse y crear/editar/publicar un iPhone con imágenes.
4. Un producto despublicado desaparece del catálogo público.
5. Una modificación admin queda registrada en auditoría.
6. El build Docker sirve SPA + API en un solo contenedor.
