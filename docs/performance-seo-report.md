# Informe de rendimiento y SEO — WA Shop

**Fecha:** 2026-07-15

## Decisiones

1. **Lazy loading de rutas públicas** (`loadComponent`) para reducir JS inicial del admin/público cruzado.
2. **Sin Node SSR en runtime** (monolito JRE): meta HTML vía Spring + `SeoService` en cliente.
3. **Prerender Angular** intentado con `@angular/ssr`; deshabilitado por NG0401. Reintento futuro posible cuando el grafo de rutas sea compatible con extract.
4. **Fuentes:** Google Fonts con `display=swap` (ya en `index.html`).
5. **Imágenes:** `alt` contextual; lazy en thumbs; dimensiones cuando existen en metadatos.
6. **Assets hashed** en build Angular (`outputHashing: all`) → cacheable en CDN/hosting.
7. **Sitemap cacheado ~60s** en backend; invalidación al cambiar catálogo/settings.
8. **Presupuesto de bundle** elevado levemente (warning 600kB) por panel admin en el mismo SPA; lazy routes mitigan ruta pública.

## Core Web Vitals (orientación)

| Métrica | Mitigaciones aplicadas | Pendiente de medición en prod |
|---------|------------------------|-------------------------------|
| LCP | Meta/HTML útil en primer documento; hero sin imagen pesada obligatoria | Lighthouse en dominio real |
| INP | Menos JS en rutas lazy; sin librerías UI pesadas | Medición RUM |
| CLS | Espacios de imagen / skeletons existentes | Revisar cards con dims fijas |

## No aplicado (riesgo / fuera de alcance)

- Service Worker agresivo (riesgo de stock/precio stale).
- CDN de imágenes transformadas más allá de Cloudinary (ya opcional).
- Eliminación total del panel del bundle público (requeriría apps separadas; no compatible con monolito actual sin decisión explícita).

## Cómo medir

1. Desplegar con `PUBLIC_SITE_URL=https://dominio`.
2. PageSpeed Insights / Lighthouse móvil y desktop en `/`, `/iphone`, ficha de producto.
3. Search Console → Core Web Vitals tras indexación.
