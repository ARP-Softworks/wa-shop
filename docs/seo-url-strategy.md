# Estrategia de URLs SEO — WA Shop

## Estructura indexable

| URL | Contenido |
|-----|-----------|
| `/` | Inicio |
| `/iphone` | Catálogo iPhone |
| `/iphone/nuevos` | Solo condición NEW |
| `/iphone/usados` | Solo condición USED |
| `/iphone/modelo/{modelSlug}` | Filtro por modelo (slug del modelo) |
| `/iphone/{productSlug}` | Detalle iPhone publicado |
| `/accesorios` | Catálogo accesorios |
| `/accesorios/categoria/{categorySlug}` | Accesorios por categoría (evita colisión con producto) |
| `/accesorios/{productSlug}` | Detalle accesorio |
| `/servicio-tecnico` | Listado |
| `/servicio-tecnico/{serviceSlug}` | Detalle servicio activo |
| `/contacto` | Contacto |
| `/preguntas-frecuentes` | FAQ |

## Redirecciones 301

| Origen | Destino |
|--------|---------|
| `/catalogo` | `/iphone` |
| `/catalogo/{slug}` | `/iphone/{slug}` |
| Cambio de slug (admin) | Entrada en `url_redirects` |

## Filtros y query params

- Filtros interactivos (`q`, `color`, `minPrice`, `sort`, `page`, etc.) **siguen funcionando**.
- Canonical de listados apunta a la URL limpia de la sección (`/iphone`, `/iphone/usados`, etc.), no a cada combinación de filtros.
- Combinaciones de filtros **no** entran al sitemap.
- Parámetros de orden: no generan páginas indexables propias.
- Paginación: enlaces reales `?page=`; canonical sigue en la URL base de la sección (política actual del backend).

## Unicidad

- Un producto = una URL canónica según `productType`.
- Categorías de accesorios usan prefijo `/accesorios/categoria/` para no chocar con slugs de producto.
- Segmentos reservados bajo `/iphone/`: `nuevos`, `usados`, `modelo`.

## Productos sin stock / retirados

| Estado | Comportamiento |
|--------|----------------|
| Publicado, stock 0 | Página 200; mensaje sin stock; Offer `OutOfStock`; permanece en sitemap si `indexable` |
| No publicado / eliminado | 404 HTML (+ `noindex`); fuera del sitemap; no redirigir a home |
| Slug cambiado | 301 vía `url_redirects` |

## Slugs

Minúsculas, guiones, sin tildes ni espacios; únicos; estables tras publicación (cambios con 301).
