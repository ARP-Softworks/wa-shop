# Estrategia SEO — WA Shop

## Objetivos

Posicionar el sitio público para búsquedas comerciales en Uruguay relacionadas con iPhone (nuevos/usados), accesorios y servicio técnico, sin keyword stuffing ni afirmaciones no verificadas sobre Apple.

## Renderizado

| Capa | Rol |
|------|-----|
| Spring `SeoHtmlDocumentFilter` | HTML inicial con title, meta, canonical, OG, Twitter, JSON-LD y status 404 real cuando corresponde |
| Angular `SeoService` | Actualización en navegación SPA |
| Prerender Angular SSR | Evaluado (Angular 19); deshabilitado tras error NG0401 en extract de rutas. Scaffold conservado. Sin Node en runtime (compatible con monolito Docker/JRE) |

## URLs

Ver `docs/seo-url-strategy.md`. Dominio: `PUBLIC_SITE_URL` / settings `publicSiteUrl` / fallback `APP_PUBLIC_BASE_URL`.

## Metadatos

- Generados en backend (`SeoPageService` / `SeoMetaBuilder`) y aplicados en cliente.
- Overrides opcionales: `seoTitle`, `metaDescription`, `indexable` en producto/categoría/servicio.
- Defaults en configuración del sitio.
- Verificación Google/Bing: meta content sanitizado (no scripts libres).

## Datos estructurados

- Inicio: `ElectronicsStore` / organización con datos reales de settings.
- Producto: `Product` + `Offer` (sin IMEI; disponibilidad según stock).
- Breadcrumbs: `BreadcrumbList`.
- Servicios: `Service` (+ `Offer` si hay precio).
- FAQ: `FAQPage` con preguntas reales de `FaqContent` (incluye aclaración de no ser servicio oficial Apple).

## Sitemap y robots

- `GET /sitemap.xml` — solo URLs públicas indexables.
- `GET /robots.txt` — Allow público; Disallow `/admin`, `/api/`; Sitemap absoluto.
- Admin y login: `noindex` en meta.

## Analítica

Variables opcionales vacías por defecto:

- `GOOGLE_ANALYTICS_ID`
- `GOOGLE_TAG_MANAGER_ID`
- `GOOGLE_SITE_VERIFICATION` / `BING_SITE_VERIFICATION` (también en settings)

Scripts solo si hay ID configurado.

## Contenido editorial

Intros naturales en iPhone / nuevos / usados / accesorios / servicio técnico / FAQ.  
No se publican reseñas ficticias ni ratings Schema inventados.
