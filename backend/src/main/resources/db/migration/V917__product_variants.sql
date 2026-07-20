-- Parent product + sellable variants (replaces sibling products linked by product_groups).

CREATE TABLE product_variants (
    id                  UUID PRIMARY KEY,
    product_id          UUID NOT NULL,
    condition           VARCHAR(20) NOT NULL,
    storage_capacity    VARCHAR(40),
    color               VARCHAR(80),
    battery_health      INT,
    price               NUMERIC(12, 2) NOT NULL,
    previous_price      NUMERIC(12, 2),
    currency            CHAR(3) NOT NULL,
    stock               INT NOT NULL DEFAULT 0,
    warranty            VARCHAR(200),
    imei                VARCHAR(20),
    published           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_product_variants_condition CHECK (condition IN ('NEW', 'USED')),
    CONSTRAINT chk_product_variants_currency CHECK (currency IN ('UYU', 'USD')),
    CONSTRAINT chk_product_variants_stock CHECK (stock >= 0),
    CONSTRAINT chk_product_variants_price CHECK (price >= 0),
    CONSTRAINT chk_product_variants_previous_price CHECK (previous_price IS NULL OR previous_price >= 0),
    CONSTRAINT chk_product_variants_battery_health CHECK (
        battery_health IS NULL OR (battery_health >= 0 AND battery_health <= 100)
    ),
    CONSTRAINT chk_product_variants_imei_length CHECK (imei IS NULL OR char_length(imei) BETWEEN 14 AND 17)
);

CREATE UNIQUE INDEX uk_product_variants_imei ON product_variants (imei);
CREATE INDEX idx_product_variants_product_id ON product_variants (product_id);
CREATE INDEX idx_product_variants_published ON product_variants (product_id, published);

ALTER TABLE product_images ADD COLUMN variant_id UUID;
ALTER TABLE order_items ADD COLUMN variant_id UUID;

INSERT INTO product_variants (
    id, product_id, condition, storage_capacity, color, battery_health,
    price, previous_price, currency, stock, warranty, imei, published,
    created_at, updated_at
)
SELECT
    id,
    id,
    condition,
    storage_capacity,
    color,
    battery_health,
    price,
    previous_price,
    currency,
    stock,
    warranty,
    imei,
    published,
    created_at,
    updated_at
FROM products;

UPDATE product_images
SET variant_id = product_id
WHERE EXISTS (SELECT 1 FROM product_variants pv WHERE pv.id = product_images.product_id);

UPDATE order_items
SET variant_id = product_id
WHERE product_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.id = order_items.product_id);

ALTER TABLE product_images ADD CONSTRAINT fk_product_images_variant
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE;

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_variant
    FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE SET NULL;

-- Point all variants in a group at the canonical (oldest) parent.
UPDATE product_variants
SET product_id = (
    SELECT p2.id
    FROM products p2
    WHERE p2.product_group_id = (
        SELECT p.product_group_id FROM products p WHERE p.id = product_variants.product_id
    )
    ORDER BY p2.created_at ASC, p2.id ASC
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM products p
    WHERE p.id = product_variants.product_id
      AND p.product_group_id IS NOT NULL
);

UPDATE product_images
SET product_id = (
    SELECT p2.id
    FROM products p2
    WHERE p2.product_group_id = (
        SELECT p.product_group_id FROM products p WHERE p.id = product_images.product_id
    )
    ORDER BY p2.created_at ASC, p2.id ASC
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM products p
    WHERE p.id = product_images.product_id
      AND p.product_group_id IS NOT NULL
);

UPDATE order_items
SET product_id = (
    SELECT p2.id
    FROM products p2
    WHERE p2.product_group_id = (
        SELECT p.product_group_id FROM products p WHERE p.id = order_items.product_id
    )
    ORDER BY p2.created_at ASC, p2.id ASC
    LIMIT 1
)
WHERE product_id IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM products p
    WHERE p.id = order_items.product_id
      AND p.product_group_id IS NOT NULL
);

UPDATE inquiries
SET product_id = (
    SELECT p2.id
    FROM products p2
    WHERE p2.product_group_id = (
        SELECT p.product_group_id FROM products p WHERE p.id = inquiries.product_id
    )
    ORDER BY p2.created_at ASC, p2.id ASC
    LIMIT 1
)
WHERE product_id IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM products p
    WHERE p.id = inquiries.product_id
      AND p.product_group_id IS NOT NULL
);

DELETE FROM product_features
WHERE product_id IN (
    SELECT member_id FROM (
        SELECT
            p.id AS member_id,
            FIRST_VALUE(p.id) OVER (
                PARTITION BY p.product_group_id ORDER BY p.created_at ASC, p.id ASC
            ) AS canonical_id
        FROM products p
        WHERE p.product_group_id IS NOT NULL
    ) ranked
    WHERE member_id <> canonical_id
);

DELETE FROM product_compatible_models
WHERE product_id IN (
    SELECT member_id FROM (
        SELECT
            p.id AS member_id,
            FIRST_VALUE(p.id) OVER (
                PARTITION BY p.product_group_id ORDER BY p.created_at ASC, p.id ASC
            ) AS canonical_id
        FROM products p
        WHERE p.product_group_id IS NOT NULL
    ) ranked
    WHERE member_id <> canonical_id
);

DELETE FROM products
WHERE id IN (
    SELECT member_id FROM (
        SELECT
            p.id AS member_id,
            FIRST_VALUE(p.id) OVER (
                PARTITION BY p.product_group_id ORDER BY p.created_at ASC, p.id ASC
            ) AS canonical_id
        FROM products p
        WHERE p.product_group_id IS NOT NULL
    ) ranked
    WHERE member_id <> canonical_id
);

-- Refresh denormalized parent listing fields from variants.
UPDATE products
SET
    price = (
        SELECT pv.price FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    previous_price = (
        SELECT pv.previous_price FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    currency = (
        SELECT pv.currency FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    stock = (
        SELECT COALESCE(SUM(pv.stock), 0) FROM product_variants pv WHERE pv.product_id = products.id
    ),
    condition = (
        SELECT pv.condition FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    storage_capacity = (
        SELECT pv.storage_capacity FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    color = (
        SELECT pv.color FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    battery_health = (
        SELECT pv.battery_health FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    warranty = (
        SELECT pv.warranty FROM product_variants pv
        WHERE pv.product_id = products.id
        ORDER BY CASE WHEN pv.published THEN 0 ELSE 1 END, pv.price ASC, pv.created_at ASC
        LIMIT 1
    ),
    imei = NULL,
    published = EXISTS (
        SELECT 1 FROM product_variants pv WHERE pv.product_id = products.id AND pv.published = TRUE
    );

ALTER TABLE products DROP CONSTRAINT IF EXISTS uk_products_imei;
