# Auditoría SEO — WA Shop

**Fecha:** 2026-07-15  
**Stack:** Angular 19.2 (CSR) + Spring Boot 3.4 monolito + PostgreSQL/Flyway  
**Mockup:** `docs/mockups/wa-shop-mockup.png` — **ausente** (solo README); la UI se contrastará con la identidad existente (verde bosque, tipografía actual).

---

## 1. Estado actual del SEO

| Área | Estado |
|------|--------|
| Títulos / meta dinámicos | Solo estáticos en `index.html` (`WA Shop` + description genérica) |
| Canonical / OG / Twitter | Ausentes |
| JSON-LD | Ausente |
| `robots.txt` / `sitemap.xml` | Ausentes (el fallback SPA serviría `index.html`) |
| SSR / prerender | No configurados |
| URLs amigables | Parcial (`/catalogo/:slug`, `/accesorios/:slug`); no hay `/iphone/nuevos\|usados`, ni FAQ, ni detalle de servicio |
| Campos SEO en admin | Solo `slug`, `description`, `altText` de imágenes |
| IMEI en público | Correctamente excluido de DTOs públicos |
| `PUBLIC_SITE_URL` | No existe; hay `APP_PUBLIC_BASE_URL` (medios/local) |

---

## 2. Rutas públicas existentes

| Ruta | Componente | Indexable hoy |
|------|------------|---------------|
| `/` | Home | Sí (contenido CSR) |
| `/catalogo` | Catálogo iPhone | Sí |
| `/catalogo/:slug` | Detalle producto | Sí si publicado |
| `/accesorios` | Catálogo accesorios | Sí |
| `/accesorios/:slug` | Detalle accesorio | Sí si publicado |
| `/servicio-tecnico` | Lista servicios | Sí |
| `/contacto` | Contacto | Sí |
| `/404` | Not found | No debería |
| `/admin/**`, `/admin/login` | Admin | No |
| `**` | Not found | No |

No existen: `/iphone/*`, `/preguntas-frecuentes`, `/servicio-tecnico/:slug`, `/iphone/modelo/:modelSlug`.

---

## 3. Problemas encontrados

1. **SPA CSR puro:** el HTML inicial no contiene el contenido ni metadatos por ruta; Google puede indexar mal y WhatsApp/Facebook no ven OG correctos.
2. **Un solo `<title>`** para todo el sitio.
3. **Sin canonical** → riesgo de duplicados por query params de filtros.
4. **Sin sitemap/robots** gestionados.
5. **URLs `/catalogo`** menos alineadas a intención de búsqueda (“iPhone Uruguay”).
6. **Alt de imágenes** usa a menudo solo el nombre del producto (mejorable).
7. **Sin página FAQ** ni intro SEO en catálogos.
8. **Sin redirecciones 301** versionadas.
9. **Sin control `indexable`** por entidad.
10. **Mockup PNG ausente** — no se puede validar layout contra referencia visual obligatoria.

---

## 4. Páginas indexables (objetivo)

- Inicio, iPhone (todos / nuevos / usados), accesorios, categorías activas indexables, servicios técnicos activos indexables, contacto, FAQ, productos publicados con `indexable=true`.

## 5. Páginas que no deben indexarse

- `/admin/**`, `/admin/login`, `/api/**`, previsualizaciones, productos no publicados, errores 404, filtros/orden con params temporales, webhooks futuros, media técnica si aplica.

---

## 6. Títulos y descripciones actuales

- Globales en `frontend/src/index.html`.
- `data.title` en rutas de catálogo es solo H1 de UI, no document title.

## 7. Contenido semántico

- Layout con `header` / `nav` / `main` / `footer` y un `h1` por página: bien encaminado.
- Faltan intros comerciales naturales en catálogos y home orientadas a Uruguay sin keyword stuffing ni afirmaciones no confirmadas sobre Apple.

## 8. Imágenes y alt

- Modelo con `altText`; UI pública suele caer a `product.name`.
- Sin lazy loading sistemático ni dimensiones reservadas en todas las vistas.

## 9. URLs

- Slugs admin con `slugify` (minúsculas, guiones).
- Estructura objetivo documentada en `docs/seo-url-strategy.md` (post-implementación).
- Migración: `/catalogo` → `/iphone` con 301.

## 10. Renderizado para buscadores

- Recarga de rutas: OK vía `SpaWebConfig` → `index.html`.
- Contenido útil en primer HTML: **no**.

## 11. Riesgos Angular SPA

- Soft 404 (200 + “no encontrado”).
- Meta solo post-JS.
- Duplicados por `?page=&sort=&q=`.
- Crawl budget en filtros infinitos.

## 12. Recomendación SSR / prerender / híbrido

**Contexto:** AGENTS.md exige **una sola imagen Docker con JRE** (sin Node.js como backend de la app). SSR dinámico oficial de Angular 19 (`@angular/ssr` + servidor Node) **no es compatible** con ese empaquetado en runtime.

**Estrategia elegida (híbrida oficial + monolito):**

1. **Prerender estático Angular 19** (`@angular/ssr` / application builder) de rutas públicas **estáticas** en build (home, iPhone, nuevos, usados, accesorios, servicio técnico, contacto, FAQ). El artefacto sigue siendo HTML/JS estático servido por Spring Boot — sin Node en producción.
2. **Enriquecimiento HTML en Spring Boot** para rutas dinámicas (producto, servicio, categoría): al servir el documento HTML se inyectan `title`, description, canonical, OG y JSON-LD con datos reales. Cubre crawlers y shares (WhatsApp/Facebook).
3. **`SeoService` en Angular** para actualizar metadatos en navegación SPA.
4. **Limitación:** el listado de productos dentro del HTML prerenderizado puede estar vacío si no hay API en build; el valor SEO del shell (H1, intro, meta) sí está presente. El detalle de producto obtiene meta completa vía Spring.

No se introduce BFF Node ni microservicios.

---

## 13. Plan de implementación priorizado

| Prioridad | Ítem |
|-----------|------|
| P0 | Migraciones SEO + settings `PUBLIC_SITE_URL` + redirects |
| P0 | `robots.txt` + `sitemap.xml` backend |
| P0 | Meta injection Spring + `SeoService` Angular |
| P0 | Nueva estructura de URLs + 301 desde `/catalogo` |
| P1 | Campos SEO admin (producto, categoría, servicio, settings) |
| P1 | JSON-LD (Organization/Store, Product, Breadcrumb, Service, FAQ si hay contenido real) |
| P1 | Contenido intro + FAQ + semántica + imágenes |
| P1 | Prerender rutas estáticas |
| P2 | Analítica opcional (GA/GTM) + verification meta |
| P2 | Performance (lazy routes, cache assets, font-display) |
| P2 | Tests + docs + validación Docker |

---

## 14. Campos SEO a administrar desde el panel

**Productos:** `seoTitle`, `metaDescription`, `slug` (ya), `alt` imágenes (ya), `indexable`, preview SERP.  
**Categorías / servicios:** `seoTitle`, `metaDescription`, `slug` (ya), descripción, `indexable`.  
**Settings:** nombre SEO, description default, `publicSiteUrl`, imagen social default, logo (ya), NAP, verificación Google/Bing (meta content, no scripts libres), GA/GTM IDs opcionales vía env o settings según diseño final (preferir env para IDs de tags; settings para verification strings).

Validación: longitudes, texto plano (sin HTML/JS), URLs http(s).
