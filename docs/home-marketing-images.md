# Imágenes de marketing — home

## Estado

Las fotos están integradas en la home con fondo transparente (PNG).

| Archivo en uso | Sección |
|----------------|---------|
| `frontend/src/assets/marketing/hero-iphones.png` | Hero |
| `category-nuevos.png` | Categoría nuevos |
| `category-usados.png` | Categoría usados |
| `category-accesorios.png` | Accesorios |
| `category-servicio.png` | Servicio técnico |

Los JPG originales se mantienen como respaldo.

## Procesamiento

1. `category-usados` y `category-servicio` llegaron como **WebP** con extensión `.jpg` → se normalizaron a JPEG real.
2. Fondo blanco removido con flood-fill desde los bordes (Sharp) → PNG con canal alfa.
3. En accesorios/nuevos (productos blancos) se usó tolerancia baja para no borrar el producto.

## Cómo reemplazar

Sobreescribí el PNG (o el JPG y pedí reprocesar) en `frontend/src/assets/marketing/` y recargá el front.
