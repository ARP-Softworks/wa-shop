# AGENTS.md — WA Shop

Instrucciones obligatorias para agentes y desarrolladores que trabajen en este repositorio.

## Objetivo del proyecto

Construir un sistema web monolítico modular para **WA Shop**, empresa uruguaya de venta de iPhone nuevos y usados, accesorios y servicios técnicos.

La primera versión incluye:

- Sitio público con catálogo, filtros, detalle de producto, consulta por WhatsApp e información de servicio técnico.
- Panel administrativo con autenticación, CRUD de productos/categorías/accesorios/servicios, gestión de consultas e imágenes, configuración del sitio, publicación/despublicación, stock básico y auditoría administrativa.

**Referencia visual obligatoria:** `docs/mockups/wa-shop-mockup.png`.

## Tecnologías obligatorias

| Capa | Tecnología |
|------|------------|
| Frontend | Angular, TypeScript, SCSS, componentes standalone, Reactive Forms, Router, HttpClient, interceptors, Signals cuando aporten valor |
| Backend | Java, Spring Boot, Spring Web, Spring Data JPA, Spring Security, Bean Validation, Maven |
| Base de datos | PostgreSQL (compatible con Neon), Flyway |
| API | REST bajo `/api` |
| Empaquetado | Una sola imagen Docker; Angular compilado e incluido en el artefacto Spring Boot |
| Medios | Cloudinary o proveedor S3-compatible; PostgreSQL solo guarda URLs y metadatos |

**No cambiar el stack sin consulta explícita al responsable del proyecto.**

## Convenciones de código

- Idioma de dominio y documentación de producto: español (Uruguay).
- Identificadores de código (clases, métodos, variables, paquetes, rutas de módulos): inglés.
- Mensajes de API orientados a UI pueden estar en español.
- Nombres claros y explícitos; evitar abreviaturas opacas.
- Commits pequeños, verificables y fáciles de revisar.
- No dejar código comentado muerto ni TODOs sin contexto.
- Preferir cambios incrementales frente a refactors masivos no solicitados.

## Reglas de arquitectura

- Monolito modular: un único backend desplegable, organizado por módulos de dominio.
- Un único frontend Angular servido por Spring Boot en producción.
- Separar capas por módulo: `api` (controllers/DTOs), `application` (casos de uso), `domain` (entidades/reglas), `infrastructure` (JPA, seguridad, storage).
- Mercado Pago debe modelarse como módulo opcional futuro; el catálogo no debe acoplarse a un proveedor de pagos.
- No introducir microservicios, BFF separados ni backends Node.js.
- No combinar Angular con Next.js ni otros frameworks de frontend.

## Reglas de seguridad

- Nunca exponer secretos, credenciales, tokens ni claves en código, commits, logs o respuestas de API.
- Variables sensibles solo por entorno / secretos del hosting.
- Endpoints administrativos protegidos por autenticación y autorización.
- IMEI y datos sensibles visibles únicamente para roles administrativos.
- Validar entrada en backend con Bean Validation; no confiar solo en el frontend.
- Sanitizar/escapar salidas donde corresponda; evitar XSS en contenido administrable.
- Usar HTTPS en producción (certificado gestionado por el proveedor de alojamiento).

## Reglas para frontend

- Componentes standalone.
- Formularios reactivos para formularios de negocio.
- Interceptors para autenticación (token/sesión) y manejo centralizado de errores HTTP.
- Signals para estado local/reactivo simple; no introducir state managers pesados sin justificación.
- SCSS modular; variables de diseño alineadas al mockup.
- Diseño responsive obligatorio (móvil, tablet, desktop).
- **Revisar el mockup antes de modificar interfaces** públicas o administrativas.
- Sin librerías UI pesadas salvo justificación documentada (tamaño, accesibilidad, mantenimiento).
- Rutas públicas y `/admin` claramente separadas.

## Reglas para backend

- API REST bajo prefijo `/api`.
- Arquitectura modular dentro de un solo artefacto Maven/Spring Boot.
- Controllers delgados; lógica de negocio en servicios de aplicación.
- DTOs de entrada/salida; no exponer entidades JPA directamente si contienen datos sensibles.
- Spring Security como único mecanismo de authn/authz del monolito.
- Transacciones explícitas en casos de uso que modifiquen varias entidades.
- Excepciones de dominio mapeadas a respuestas HTTP consistentes.

## Reglas de base de datos

- PostgreSQL como única base relacional.
- Compatible con Neon (SSL, connection pooling vía URL/params).
- Solo URLs y metadatos de imágenes en DB; **prohibido almacenar binarios de imágenes en PostgreSQL**.
- Claves foráneas e índices para filtros frecuentes (estado, categoría, publicación, destacado).
- Soft constraints de negocio documentadas en migraciones y en `docs/database-model.md`.

## Reglas de migraciones

- Flyway es la única fuente de verdad del esquema.
- No usar `ddl-auto=update` en entornos compartidos/producción.
- Migraciones versionadas, incrementales e idempotentes en su aplicación.
- No editar migraciones ya aplicadas en entornos compartidos; crear una nueva.
- Incluir seed de desarrollo en scripts separados o migraciones claramente marcadas como demo (no productivas).

## Reglas de pruebas

- Antes de finalizar una tarea: ejecutar compilación y pruebas relevantes.
- Backend: pruebas unitarias de reglas de negocio y pruebas de integración de API críticas.
- Frontend: pruebas de componentes/servicios en flujos críticos (auth, catálogo, CRUD admin).
- No marcar una historia como lista si rompe el build.

## Reglas de manejo de errores

- Respuestas de error de API con formato consistente (`timestamp`, `status`, `error`, `message`, `path`, `details` opcional).
- No filtrar stack traces ni datos internos al cliente.
- Registrar errores de servidor con correlación suficiente para diagnóstico.
- Errores de validación → `400`; no autenticado → `401`; sin permiso → `403`; no encontrado → `404`; conflicto de negocio → `409`.

## Reglas de autenticación

- Solo administradores autentican en la v1 (no hay cuentas de clientes finales).
- Credenciales nunca en logs.
- Tokens/sesiones con expiración; renovación o re-login documentado.
- Rutas `/api/admin/**` (y equivalentes) requieren rol administrativo.
- Rutas públicas de catálogo sin autenticación, con proyección segura (sin IMEI).

## Reglas de despliegue

- Una sola imagen Docker publica frontend + backend.
- Angular se compila y se copia al `classpath` estático de Spring Boot.
- Spring Boot sirve la SPA y reenvía rutas no-API al `index.html`.
- Configuración por variables de entorno; documentar cada variable nueva en README y `docs/deployment.md`.
- SSL terminado en el proveedor de hosting; la app asume HTTPS público.
- Healthcheck HTTP expuesto para orquestación.

## Prohibiciones explícitas

1. **No** introducir microservicios.
2. **No** almacenar imágenes binarias en PostgreSQL.
3. **No** eliminar o renombrar variables de entorno existentes sin justificación explícita y actualización de documentación.
4. **No** cambiar tecnologías del stack sin consulta.
5. **No** usar Node.js como backend de la aplicación.
6. **No** combinar Angular con Next.js.
7. **No** commitear secretos ni archivos `.env` con credenciales reales.

## Obligaciones explícitas

1. Revisar el mockup (`docs/mockups/wa-shop-mockup.png`) antes de modificar interfaces.
2. Mantener el diseño responsive.
3. Ejecutar compilación y pruebas antes de finalizar una tarea.
4. Documentar variables de entorno nuevas.
5. No exponer secretos ni credenciales.
6. Realizar cambios pequeños, verificables y fáciles de revisar.
7. Mantener Mercado Pago como módulo opcional desacoplado del catálogo.

## Reglas SEO (permanentes)

- Toda página pública nueva debe definir `title`, `description`, `canonical` y `robots`.
- Toda página indexable debe evaluarse para el sitemap (`/sitemap.xml`).
- Todo contenido dinámico público debe excluir información sensible (IMEI, secretos, datos admin).
- Ningún endpoint público puede exponer IMEI.
- Las imágenes públicas deben tener texto alternativo (`alt`) descriptivo.
- No crear páginas masivas solo para palabras clave o ciudades donde no se opera.
- No generar reseñas ni calificaciones ficticias (ni en UI ni en Schema.org).
- No afirmar relación oficial con Apple sin confirmación documentada.
- No cambiar URLs publicadas sin redirección permanente (301_redirects / 301).
- No introducir scripts arbitrarios desde la administración (solo textos/meta de verificación sanitizados).
- Todo cambio SEO debe mantener responsive, accesibilidad y rendimiento razonables.
- El dominio canónico se configura con `PUBLIC_SITE_URL` (o URL pública en settings), no hardcodeado en múltiples archivos.

## Alcance fuera de v1 (no implementar como obligatorio)

- Carrito de compra
- Envíos automatizados
- Facturación / integración contable
- Aplicación móvil
- Integración obligatoria con Mercado Pago
- Microservicios
