# Plan de implementación — WA Shop

## 1. Estado actual

- Repositorio inicializado.
- Documentación de diseño creada.
- Scaffolding de código **pendiente**.
- Mockup esperado en `docs/mockups/wa-shop-mockup.png` (obligatorio antes de UI).

## 2. Orden recomendado de implementación

### Fase 0 — Preparación (esta fase)

1. Documentación (`AGENTS`, README, architecture, RF, DB, API, deploy, plan).
2. Confirmar decisiones abiertas (sección 4).
3. Ubicar/validar mockup visual.
4. `.gitignore`, estructura vacía `frontend/` / `backend/` / `scripts/`.

### Fase 1 — Cimientos

1. Scaffold Spring Boot (Maven, módulos base, `application.yml`, health).
2. Scaffold Angular (standalone, routing público/admin, SCSS base según mockup).
3. Docker multi-stage + script de build integrado.
4. Flyway: `admin_users`, security JWT, login admin.
5. Interceptors Angular (auth + errores).

**Salida:** login admin funciona; SPA servida desde JAR en build integrado.

### Fase 2 — Catálogo iPhone

1. Migraciones `categories`, `products`, `product_images`.
2. API pública de listado/detalle/filtros (sin IMEI).
3. API admin CRUD + publish/unpublish + stock + featured.
4. UI pública catálogo + detalle.
5. UI admin productos/categorías.
6. Auditoría en mutaciones de catálogo.

**Salida:** catálogo usable end-to-end con datos reales.

### Fase 3 — Medios

1. Adapter Cloudinary **o** S3 (según decisión).
2. Upload desde admin; asociación a productos.
3. Reordenado / imagen principal / borrado remoto.

### Fase 4 — Accesorios y servicio técnico

1. Tablas + APIs + UI pública/admin.
2. Filtros de accesorios.
3. Página de servicio técnico.

### Fase 5 — Consultas, WhatsApp y settings

1. Deep link WhatsApp + registro de `inquiries`.
2. Panel de gestión de consultas.
3. `site_settings` y consumo en layout público.

### Fase 6 — Endurecimiento MVP

1. Seeds de desarrollo.
2. Pruebas de integración API críticas.
3. Pruebas frontend de flujos clave.
4. Pulido responsive vs mockup.
5. Checklist de despliegue Neon + Docker.

### Fase 7 — Preparación etapa 2 (sin implementar MP)

1. Puerto `PaymentPort` + módulo `payments` vacío.
2. Notas de integración Mercado Pago en docs.
3. No acoplar catálogo a MP.

## 3. Entregables por fase (verificación)

| Fase | Verificación mínima |
|------|---------------------|
| 1 | `mvn test`, `npm test` smoke; login 200; JAR sirve index |
| 2 | Filtros públicos; CRUD admin; IMEI ausente en JSON público |
| 3 | Upload visible en Cloudinary/S3; URL en DB |
| 4 | Accesorios y servicios publicados visibles |
| 5 | Click WhatsApp + inquiry en admin; settings editables |
| 6 | Docker run + Neon; auditoría presente |

## 4. Decisiones que necesitan confirmación

Antes de avançar código de producción, confirmar:

| # | Decisión | Opciones | Recomendación provisional |
|---|----------|----------|---------------------------|
| D1 | Proveedor de imágenes | Cloudinary vs S3-compatible | **Cloudinary** (menos fricción inicial) |
| D2 | Auth | JWT Bearer vs cookie httpOnly session | **JWT Bearer** |
| D3 | Versiones | Angular 19/20; Spring Boot 3.4/3.5; Java 21 | **Java 21 + Spring Boot 3.4+ + Angular LTS actual** |
| D4 | Moneda por defecto | UYU vs USD vs ambas | **Ambas; default UYU** |
| D5 | Stock 0 | Ocultar / mostrar “agotado” / permitir consulta | **Mostrar agotado + permitir WhatsApp** |
| D6 | Borrado de productos | Hard delete vs soft delete | **Hard delete** en v1 |
| D7 | Accesorios | Tabla separada vs polimórfica | **Tabla separada** |
| D8 | Hosting | Railway, Render, Fly.io, VPS, otro | A definir por el usuario |
| D9 | Dominio / WhatsApp | Número y textos reales | A definir |
| D10 | Validación precio anterior | ¿obligar `previous > current`? | **Sí** |
| D11 | Bootstrap admin | Solo env vars vs usuario seed | **Env vars en primer boot** |
| D12 | Actuator | Exponer health bajo `/api/health` custom vs actuator | **Endpoint custom simple** en v1 |

## 5. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Mockup ausente o desactualizado | Bloquear UI hasta tener `wa-shop-mockup.png` |
| Scope creep (carrito/MP) | Mantener fuera de v1; solo puerto payments |
| Secretos en git | `.gitignore` + revisión de PRs |
| Build Angular+Maven frágil | Script único + CI temprano |
| Neon cold start | Pooler + timeouts documentados |

## 6. Definición de “hecho” para una tarea

1. Código alineado a `AGENTS.md`.
2. Docs de env actualizadas si hubo variables nuevas.
3. Compilación OK.
4. Pruebas relevantes OK.
5. Cambio pequeño y revisable.
6. UI contrastada con mockup cuando aplique.
