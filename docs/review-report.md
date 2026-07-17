# Informe de revisión integral — WA Shop

**Fecha:** 2026-07-15  
**Alcance:** arquitectura, seguridad, base de datos, frontend, calidad, pruebas y despliegue.  
**Regla:** no se agregaron funcionalidades nuevas en esta revisión; solo análisis y correcciones de hallazgos críticos/altos.

---

## Resumen ejecutivo

El monolito Angular + Spring Boot está mayormente alineado con `AGENTS.md`: auth por sesión/CSRF, IMEI solo admin, medios fuera de PostgreSQL, Docker multi-stage y healthchecks.  

Los riesgos más serios son de **configuración** (perfil `local` por defecto con seed demo) y de **rendimiento/integridad de datos en listados** (N+1). También hay fuga menor de metadatos de storage (`publicId`) en API pública, contrato API desactualizado (JWT) y fugas de suscripciones en Angular.

| Severidad | Cantidad (inicial) |
|-----------|--------------------|
| Crítico   | 1 |
| Alto      | 7 |
| Medio     | 10 |
| Bajo      | 8 |

---

## Crítico

### C1. Perfil Spring por defecto `local` (seed demo + cookies inseguras)

- **Evidencia:** `application.yml` → `SPRING_PROFILES_ACTIVE:local`; `application-local.yml` carga `db/dev`; `V900__dev_seed.sql` crea `admin@washop.uy` / `ChangeMe123!`.
- **Riesgo:** un arranque sin variables (JAR en servidor, compose mal tipado) puede aplicar seed demo y cookies `Secure=false` sobre una base real.
- **Mitigación parcial:** Dockerfile fija `production`.
- **Estado:** corregido en esta revisión (sin perfil por defecto; `local`/`production` explícitos).

---

## Alto

### A1. N+1 de imágenes principales en listados

- **Evidencia:** `PublicCatalogService.loadPrimaryImageUrls`, `AdminProductService.loadPrimaryImages`, `AdminDashboardService` — 1–2 queries por producto.
- **Impacto:** degradación con páginas de 12–48 ítems.
- **Estado:** corregido (carga batch por `productId IN (...)`).

### A2. N+1 de producto en listados de consultas

- **Evidencia:** `InquiryMapper.toAdminSummary` accede a `product` LAZY; `AdminInquiryService.search` / dashboard sin `@EntityGraph`.
- **Estado:** corregido (`@EntityGraph(attributePaths = "product")` en repositorio).

### A3. `publicId` de storage expuesto en API pública de producto

- **Evidencia:** `ProductMapper.toPublicResponse` → `ProductImageResponse.publicId`; modelo público FE lo tipa.
- **Impacto:** metadatos de proveedor en superficie pública (no es IMEI, pero innecesario).
- **Estado:** corregido (proyección pública sin `publicId`).

### A4. Contrato API documenta JWT Bearer (desalineado con la implementación)

- **Evidencia:** `docs/api-contract.md` §1–2 (Bearer/JWT); app real usa cookie `WASESSION` + CSRF.
- **Impacto:** confusión operativa y de seguridad.
- **Estado:** corregido (documentación alineada a sesión + CSRF).

### A5. Build Docker omite tests (`-DskipTests`)

- **Evidencia:** `Dockerfile` stage Maven.
- **Impacto:** imagen publicable con tests rotos.
- **Estado:** corregido (`mvn test` antes del package con frontend).

### A6. Suscripciones sin teardown en catálogo (y listados admin similares)

- **Evidencia:** `catalog-page.component.ts` — `queryParamMap` / `valueChanges` sin `takeUntilDestroyed` / `OnDestroy`.
- **Impacto:** fugas y cargas duplicadas al reentrar rutas.
- **Estado:** corregido en catálogo y listados admin de productos/consultas.

### A7. Mockup visual obligatorio ausente

- **Evidencia:** `docs/mockups/README.md`; no existe `wa-shop-mockup.png`.
- **Impacto:** incumplimiento de AGENTS.md para UI; no se puede “corregir” inventando el mockup.
- **Estado:** documentado; se mantiene como pendiente de activo de diseño (no inventar PNG).

---

## Medio

### M1. Acoplamiento cruzado `media` ↔ `product` / `settings`

`MediaApplicationService` consulta repos de producto/settings; servicios admin llaman a media. No hay ciclo de beans, pero sí frontera modular débil.

### M2. Lógica/persistencia en controllers públicos

`PublicSettingsController`, `PublicCategoryController`, `PublicTechnicalServiceController` usan repositorios directamente.

### M3. Capa `application` depende de `api.dto`

Inversión de dependencia respecto al ideal api → application → domain.

### M4. Lockout de login solo en memoria

`LoginAttemptService` no sobrevive reinicios ni multi-instancia.

### M5. Password JDBC por defecto `washop` en `application.yml`

Seguro solo si producción siempre sobreescribe env.

### M6. Delete de media externo dentro de la misma transacción JPA

`tryDeleteIfUnreferenced` puede hacer I/O Cloudinary con TX abierta.

### M7. Sin unicidad DB de “una sola imagen principal”

Regla solo en aplicación (`ProductImageRules`).

### M8. Categorías/servicios admin sin paginación

`findAll()` completo.

### M9. Migraciones con `IF EXISTS` (V7/V8)

Idempotencia laxa; puede ocultar drift fuera de Flyway.

### M10. V900 (dev) / V901 (prod) versión frágil al clonar DBs

Orden documentado; clonar prod→local puede chocar con seed V900.

---

## Bajo

### B1. Paquetes vacíos `accessory` / `catalog`

### B2. `CategoryResponse` público incluye timestamps de auditoría

### B3. Wildcards `%`/`_` en búsqueda `LIKE` (no es SQLi; sí DoS de resultados)

### B4. CSRF cookie no HttpOnly (requerido por Angular; sesión sí HttpOnly)

### B5. Credenciales demo documentadas (aceptable si solo perfil `local`)

### B6. Duplicación de helpers de imagen primaria (mitigada parcialmente con batch)

### B7. FE tipa dinero como `number` (backend `BigDecimal` OK)

### B8. Cobertura de tests FE limitada (casi sin CRUD admin / guards)

---

## Checklist por área

### Arquitectura (AGENTS.md)

| Ítem | Resultado |
|------|-----------|
| Monolito modular | Cumple (paquetes por dominio) |
| DTOs públicos vs admin / IMEI | Cumple (tests de integración) |
| Sin microservicios / Next / Node backend | Cumple |
| Mercado Pago desacoplado | Cumple (no implementado) |
| Controllers delgados | Parcial (M2) |
| Sin lógica sensible en Angular | Cumple |

### Seguridad

| Ítem | Resultado |
|------|-----------|
| `/api/admin/**` → `ROLE_ADMIN` | Cumple |
| BCrypt | Cumple |
| Cookies HttpOnly + Secure/SameSite en prod | Cumple |
| CSRF | Cumple |
| CORS off en production | Cumple |
| Validación uploads (magic bytes) | Cumple |
| Sin secretos en git / Angular | Cumple |
| IMEI no público | Cumple |
| Errores sin stack al cliente | Cumple |
| Perfil por defecto seguro | Corregido (C1) |

### Base de datos

| Ítem | Resultado |
|------|-----------|
| Flyway ordenado | Cumple (con nota M10) |
| Índices / constraints / BigDecimal | Cumple |
| Transacciones en writes | Cumple |
| N+1 | Corregido (A1, A2) |
| Paginación productos/consultas | Cumple |
| Borrado media con refs | Cumple |

### Frontend

| Ítem | Resultado |
|------|-----------|
| Responsive / forms / estados UI / guards | Cumple en lo esencial |
| Suscripciones | Corregido en rutas críticas (A6) |
| Mockup | Pendiente (A7) |

### Despliegue

| Ítem | Resultado |
|------|-----------|
| Docker multi-stage, health, SPA, Neon/Cloudinary docs | Cumple |
| Tests en build de imagen | Corregido (A5) |

---

## Plan de corrección aplicado (críticos + altos)

1. Eliminar perfil por defecto `local`; exigir `SPRING_PROFILES_ACTIVE` explícito en local/prod.
2. Batch de imágenes primarias + EntityGraph en inquiries.
3. DTO/mapper público sin `publicId`.
4. Actualizar `docs/api-contract.md` a sesión + CSRF.
5. Ejecutar `mvn test` en el Dockerfile antes del package.
6. `takeUntilDestroyed` / teardown en catálogo y listados admin.
7. Dejar A7 (mockup) como pendiente de activo externo.

---

## Verificación (ejecutada 2026-07-15)

| Comando | Resultado |
|---------|-----------|
| `frontend`: `npm test -- --watch=false` | **OK** — 10/10 SUCCESS |
| `frontend`: `npm run build -- --configuration=production` | **OK** — bundle generado (warnings de budget Sass, no errores) |
| `backend`: `mvn -B test` | **OK** — Tests run: 43, Failures: 0 |
| `backend`: `mvn -B -Pwith-frontend package -DskipTests` | **OK** — JAR `wa-shop-0.0.1-SNAPSHOT.jar` con assets SPA |

Pendiente externo (A7): aportar `docs/mockups/wa-shop-mockup.png` (no inventado en esta revisión).