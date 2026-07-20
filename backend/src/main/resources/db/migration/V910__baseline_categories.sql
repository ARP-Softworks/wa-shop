-- Baseline categories for a new deployment (idempotent: safe to run on a DB that
-- already has categories, or where the admin renamed/removed some of these).
-- Uses INSERT ... WHERE NOT EXISTS instead of ON CONFLICT for H2/Postgres portability.

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444401', 'iPhone', 'iphone', 'iPhone nuevos y usados', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'iphone');

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444402', 'Accesorios', 'accesorios', 'Accesorios para iPhone', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'accesorios');

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444403', 'Fundas', 'fundas', 'Fundas y carcasas', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'fundas');

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444404', 'Cargadores y cables', 'cargadores-y-cables', 'Cargadores, cables y power banks', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'cargadores-y-cables');

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444405', 'Auriculares', 'auriculares', 'Auriculares y audio', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'auriculares');

INSERT INTO categories (id, name, slug, description, active, indexable, created_at, updated_at)
SELECT '44444444-4444-4444-4444-444444444406', 'Protectores de pantalla', 'protectores-de-pantalla', 'Vidrios y láminas protectoras', TRUE, TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'protectores-de-pantalla');
