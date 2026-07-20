# Imágenes de marketing (home / marca)

Assets **fijos** del frontend: se incluyen en el build de Angular y en la imagen Docker.
Disponibles en local, staging y producción sin Cloudinary ni URLs externas.

## Archivos en uso (web)

| Archivo | Uso |
|---------|-----|
| `hero-iphones-pedestal.png` | Hero (iPhones sobre pedestal, estilo mockup) |
| `hero-iphones.png` / `.jpg` | Respaldo (composición alternativa) |
| `category-nuevos.png` | Card iPhone nuevos |
| `category-usados.png` | Card iPhone usados |
| `category-accesorios.png` | Accesorios |
| `category-servicio.png` | Servicio técnico |

Rutas en código: `assets/marketing/category-*.png` (home).

## Hero

`hero-iphones-pedestal.png` se generó recortando el mockup `docs/mockups/wa-shop-mockup-web-publica.png`.

Para nitidez HD: exportá la zona de iPhones+pedestal del mockup (ideal ≥1200×900) y reemplazá ese archivo.

## Reemplazar una categoría

Sobreescribí el `.png` correspondiente en esta carpeta (mismo nombre) y redeploy.
Los `.jpg` son respaldos opcionales; la home usa los `.png`.
