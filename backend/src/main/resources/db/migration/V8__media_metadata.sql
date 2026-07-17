-- Optional media metadata on product images; logo public id for safe deletion
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS format VARCHAR(20);
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS size_bytes BIGINT;
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS width INT;
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS height INT;

ALTER TABLE site_settings ADD COLUMN IF NOT EXISTS logo_public_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_product_images_public_id ON product_images (public_id);
