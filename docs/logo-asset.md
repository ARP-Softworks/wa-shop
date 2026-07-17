# Logo WA Shop — assets

## Archivos

| Archivo | Descripción |
|---------|-------------|
| `frontend/src/assets/logo/logo_wa-shop.jpg` | Original. No modificar. Fondo blanco cuadrado con disco circular del logo. |
| `frontend/src/assets/logo/logo_wa-shop-transparent.png` | Versión con canal alfa para UI (header, footer, admin, login, favicon). |

## Generación del PNG transparente

Fecha: 2026-07-16

Herramienta: **Jimp 0.22.12** (Node.js), ejecutada de forma puntual (`npm install --no-save jimp`), **sin** añadir la dependencia al `package.json` del frontend.

Procedimiento:

1. Lectura de `logo_wa-shop.jpg` (150×150).
2. Cálculo del círculo inscrito (centro = mitad del canvas, radio ≈ `min(w,h)/2 - 1`).
3. Fuera del círculo: alpha = 0 (esquinas blancas del JPG eliminadas).
4. Borde del círculo: anti-alias suave (~1–2 px) para evitar dientes.
5. Interior del círculo: alpha = 255 (se conserva el disco blanco del diseño y todo el arte).
6. Exportación PNG con canal alfa real.

No se usó `mix-blend-mode` ni máscaras CSS para “simular” transparencia.

## Uso en la app

Ruta de asset: `assets/logo/logo_wa-shop-transparent.png`

- Header público
- Footer
- Sidebar admin
- Login
- Favicon / apple-touch-icon (`index.html`)
