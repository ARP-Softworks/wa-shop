# Informe de implementación SEO — WA Shop

**Fecha:** 2026-07-15

## Resumen

Se implementó una estrategia SEO completa alineada al monolito Angular 19 + Spring Boot (sin Node en runtime): metadatos en HTML vía Spring, actualización SPA vía `SeoService`, sitemap/robots, URLs amigables, campos SEO en admin, JSON-LD, FAQ y documentación operativa.

## Cambios realizados

### Backend
- Migraciones `V902__seo_fields.sql`, `V903__url_redirects.sql` (después de `V901` existente).
- Módulo `uy.washop.seo`: sitemap, robots, config/page públicas, filtros HTML + redirects, JSON-LD, FAQ.
- Campos SEO en Product, Category, TechnicalService, SiteSettings.
- Redirecciones 301 `/catalogo` → `/iphone` (filtro + tabla `url_redirects`).
- DTOs públicos sin IMEI; SEO fields opcionales en APIs públicas/admin.

### Frontend
- Rutas `/iphone/*`, FAQ, detalle de servicio; lazy `loadComponent`.
- `SeoService`, `SeoApiService`, `AnalyticsService` (GA/GTM opcionales).
- Contenido intro, breadcrumbs, mejoras de alt/imágenes.
- Fieldsets SEO en formularios admin (producto, categoría, servicio, settings).
- Prerender Angular: intentado; deshabilitado (NG0401). Scaffold conservado.

### Documentación
- `docs/seo-audit.md`, `seo-strategy.md`, `seo-url-strategy.md`, `performance-seo-report.md`, este informe.
- Actualizados: `README.md`, `docs/deployment.md`, `.env.example`, `AGENTS.md` (reglas SEO).

## Migraciones

| Versión | Contenido |
|---------|-----------|
| V902 | seo_title, meta_description, indexable, published_at, settings SEO/NAP |
| V903 | tabla `url_redirects` |

## Metadatos

Title, description, robots, canonical, Open Graph, Twitter Cards, verification meta (Google/Bing).

## Datos estructurados

ElectronicsStore/Organization (datos reales), Product+Offer, BreadcrumbList, Service, FAQPage (contenido real `FaqContent`).

## Sitemap / robots

- `/sitemap.xml` — URLs indexables absolutas + lastmod.
- `/robots.txt` — Allow público; Disallow `/admin`, `/api/`; Sitemap absoluto.

## URLs

Ver `docs/seo-url-strategy.md`. Canonical vía `PUBLIC_SITE_URL`.

## Rendimiento

Lazy routes públicas; fonts `display=swap`; sin SSR Node. Detalle en `docs/performance-seo-report.md`.

## Pruebas ejecutadas

| Comando | Resultado |
|---------|-----------|
| `frontend` `npm test` | 12/12 OK |
| `frontend` `ng build --configuration=production` | OK |
| `backend` `mvn clean test` | 49 tests OK |
| `mvn -Pwith-frontend package` | OK |
| `docker compose --profile full up --build` | OK (healthy) |

## Validación HTTP en Docker (localhost:8080)

| Check | Resultado |
|-------|-----------|
| `/api/actuator/health` | UP |
| `/robots.txt` | 200, Disallow admin/api, Allow / |
| `/sitemap.xml` | 200 `application/xml`, locs absolutas `/`, `/iphone`, … |
| `/` HTML | title + canonical + JSON-LD Organization/ElectronicsStore |
| `/iphone` HTML | title `iPhone en Uruguay \| WA Shop` + canonical |
| `/catalogo` | **301** → `http://localhost:8080/iphone` |
| `/admin/login` | `noindex, nofollow` |
| `/api/public/products` | sin campo `imei` |
| `/api/admin/dashboard` | **401** |

## Pendientes / limitaciones

1. **Mockup PNG** ausente — no se pudo contrastar UI vs `wa-shop-mockup.png`.
2. **Prerender Angular** deshabilitado (NG0401); HTML útil para crawlers vía Spring `SeoHtmlDocumentFilter`.
3. **Lighthouse/CWV en producción** — medir tras desplegar dominio real.
4. **Inspección visual móvil/desktop y consola del navegador** — pendiente manual en el dominio desplegado.
5. Confirmar línea `Sitemap:` en `robots.txt` — **verificado** en Docker: `Sitemap: http://localhost:8080/sitemap.xml`.
6. Ficha producto HTML — **verificado**: title + canonical + JSON-LD `Product`/`Offer` sin IMEI.

## Pasos del propietario del sitio

1. Configurar `PUBLIC_SITE_URL=https://tu-dominio` (HTTPS).
2. Completar Admin → Configuración (NAP, horarios, imagen social, títulos default).
3. Google Business Profile con los mismos datos NAP.
4. Search Console: verificar + enviar `https://tu-dominio/sitemap.xml`.
5. Bing Webmaster: verificar + enviar sitemap.
6. Opcional: `GOOGLE_ANALYTICS_ID` / `GOOGLE_TAG_MANAGER_ID`.
7. Revisar títulos SEO por producto destacado; no marcar `indexable=false` salvo justificación.

## Search Console — instrucciones breves

1. Añadir propiedad de dominio o prefijo URL.
2. Pegar código en settings (`googleSiteVerification`) o DNS.
3. Sitemaps → agregar `sitemap.xml`.
4. Inspeccionar URL de inicio e `/iphone` tras el primer crawl.

## Google Business Profile

Crear/editar ficha; nombre, dirección, teléfono idénticos al sitio; categoría electrónica/celulares; enlace al sitio; no afirmar ser Apple Store.
