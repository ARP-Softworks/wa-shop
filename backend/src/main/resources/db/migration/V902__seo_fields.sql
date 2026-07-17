-- SEO fields for products, categories, technical services, and site settings.
-- Versioned after V901 (Hibernate column alignment) to apply on existing DBs.
-- One ADD COLUMN per statement for H2 (PostgreSQL MODE) compatibility.

ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_title VARCHAR(70);
ALTER TABLE products ADD COLUMN IF NOT EXISTS meta_description VARCHAR(320);
ALTER TABLE products ADD COLUMN IF NOT EXISTS indexable BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE categories ADD COLUMN IF NOT EXISTS seo_title VARCHAR(70);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS meta_description VARCHAR(320);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS indexable BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE technical_services ADD COLUMN IF NOT EXISTS seo_title VARCHAR(70);
ALTER TABLE technical_services ADD COLUMN IF NOT EXISTS meta_description VARCHAR(320);
ALTER TABLE technical_services ADD COLUMN IF NOT EXISTS indexable BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS public_site_url VARCHAR(500);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS default_seo_title VARCHAR(70);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS default_meta_description VARCHAR(320);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS default_social_image_url TEXT;
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS google_site_verification VARCHAR(120);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS bing_site_verification VARCHAR(120);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS country VARCHAR(80);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS region VARCHAR(120);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS city VARCHAR(120);
ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS postal_code VARCHAR(40);

UPDATE products
SET published_at = COALESCE(published_at, created_at)
WHERE published = TRUE AND published_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_products_published_indexable
    ON products (published, indexable);

CREATE INDEX IF NOT EXISTS idx_categories_active_indexable
    ON categories (active, indexable);

CREATE INDEX IF NOT EXISTS idx_technical_services_active_indexable
    ON technical_services (active, indexable);
