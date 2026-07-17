# Diff visual — home vs mockup público

**Referencia:** `docs/mockups/wa-shop-mockup-web-publica.png`  
**Validación browser:** `http://localhost:4200/?v=fix3`  
**Fecha:** 2026-07-16

## Diferencias corregidas

1. Contenedor ampliado a `--layout-max: 90rem` (~1440px).
2. Header: logo real más grande, sin texto “WA Shop” separado; altura compacta.
3. Hero más bajo: copy + imagen grande; texto oficial del mockup; “WA Shop” en azul.
4. Trust bar con 5 ítems incluyendo **Envíos a todo el país** (solo informativo, sin módulo de envíos).
5. Sin eyebrows CATEGORÍAS/DESTACADOS; títulos centrados / fila con link.
6. Categorías compactas (altura de imagen fija ~7.5rem); bug de cards usados/servicio ocultando texto corregido.
7. Destacados: grilla 5 columnas; cards compactas; link “Ver todos…”.
8. Beneficios: franja horizontal de 5 ítems.
9. Opiniones: estructura + demos solo si `!environment.production`; en prod se oculta si no hay datos.
10. Footer multi-columna (nav, categorías, info, contacto, horario).

## Archivos modificados

- `frontend/src/styles.scss`
- `frontend/src/app/layouts/public-layout/*`
- `frontend/src/app/features/public/home/*`
- `frontend/src/app/shared/components/product-card/*`
- `docs/home-visual-diff.md`

## Lo que aún no puede coincidir 1:1

| Elemento | Motivo |
|----------|--------|
| 5 productos destacados en fila | Seed/backend solo tiene 1 featured; no se inventan productos |
| Foto hero del mockup (pedestal exacto) | Usamos el asset del proyecto (`hero-iphones.png`) |
| Dropdowns “iPhone/Accesorios” y “Nosotros” | Rutas reales sin submenús |
| Opiniones reales | Solo demos locales marcadas “Demo local” |
| Trust bar en 1 sola fila en viewports &lt; ~1200px | Se reacomoda en wrap sin scroll horizontal |

## Validación

- Tests Angular: 12/12
- Build producción: OK
- Captura de comparación: browser Cursor en `/` tras los ajustes (`?v=fix3`)
