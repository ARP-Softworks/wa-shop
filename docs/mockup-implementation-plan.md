# Plan de implementación visual — Mockup WA Shop

**Fecha:** 2026-07-16  
**Referencias:** `docs/mockups/wa-shop-mockup.png`, `frontend/src/assets/logo/logo_wa-shop.jpg`

---

## 1. Diferencias actuales vs mockup

| Área | Actual | Mockup |
|------|--------|--------|
| Paleta | Verde bosque (`#1f7a54`), fondos verdosos | Blanco, negro/gris oscuro, **azul** como acento |
| Tipografía | Sora + Source Sans 3 | Sans-serif limpia tipo Inter/sistema tech |
| Header | Marca “WA” textual, nav comprimido, CTA WhatsApp verde | Logo real, nav horizontal limpia, CTA azul WhatsApp |
| Hero | Panel decorativo abstracto verde | Título fuerte + CTAs + composición iPhone |
| Categorías | Cards texto planas | 4 tarjetas con imagen/ícono, bullets, “Ver más” |
| Product cards | Estilo forest, badges verdes | Cards blancas, badge Nuevo azul / Usado violeta, CTA azul |
| Beneficios | 3 artículos | Fila de íconos: garantía, revisados, segura, ST, atención |
| Opiniones | Eliminadas (correcto: sin datos reales) | Mockup muestra reseñas — **no inventar**; sección omitida |
| Footer | Claro/verde | Fondo oscuro, columnas, logo |
| Admin sidebar | Texto plano, no logo | Sidebar oscura + logo + íconos, activo azul |
| Dashboard | Cards verdes | KPI cards azul/verde/naranja/azul oscuro |
| Login | Genérico | Logo centrado, card limpia |

## 2. Componentes a modificar

- `styles.scss` + `index.html` (tokens, tipografía Inter)
- `public-layout` (header/footer + logo)
- `admin-layout` (sidebar oscura + logo)
- `home-page`
- `product-card`
- `catalog-page` (+ filtros visuales)
- `product-detail-page`
- `technical-service-page` / detail / contact / faq / not-found
- `login-page`
- `admin-dashboard-page`
- `admin-product-list` / form (+ shared `admin-page.scss`)
- Category / service / inquiry / settings / audit pages (estilos compartidos)

## 3. Componentes a conservar (lógica)

- Todos los `*ApiService`, auth, guards, SeoService, Analytics, pipes (`money`, `conditionLabel`), modelos, rutas.
- Estructura de formularios y bindings; solo HTML/SCSS y wrappers visuales.

## 4. Assets

| Asset | Uso |
|-------|-----|
| `logo_wa-shop.jpg` | Original (no sobrescribir) |
| `logo_wa-shop-transparent.png` | Header, footer, admin, login (si se logra canal alfa) |
| Hero iPhone | Ilustración CSS/SVG abstracta o imagen placeholder local sin stock falso de marca |

## 5. Estrategia del logo

1. Analizar JPG: fondo blanco + círculo con manzana/WA/SHOP.
2. Generar PNG con transparencia **fuera del círculo** (y dentro del círculo mantener blanco del disco si es parte del diseño).
3. Si no hay herramienta fiable: usar JPG en contenedor circular con padding y fondo controlado; documentar.

## 6. Responsive

- Desktop: header full, filtros superiores/grid, sidebar admin fija.
- Tablet: nav compacta, filtros en panel.
- Móvil: drawer nav, filtros acordeón/drawer, tablas → cards, sidebar overlay.

Breakpoints: 360 / 390 / 768 / 1024 / 1366 / 1440.

## 7. Orden de implementación

1. Tokens globales + tipografía  
2. Logo asset  
3. Layout público + footer  
4. Home + product-card  
5. Catálogo + detalle  
6. Login + admin layout  
7. Dashboard + tablas/formularios (admin-page.scss)  
8. Páginas restantes alineadas  
9. Pruebas / build / informe  

## 8. Fuera de alcance

Backend, rutas/API, Mercado Pago, reseñas ficticias, claims Apple oficiales, renombrar variables/servicios.
