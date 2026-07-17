-- Products (iPhone NEW/USED and ACCESSORY via product_type)
CREATE TABLE products (
    id                  UUID PRIMARY KEY,
    slug                VARCHAR(220) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    model               VARCHAR(120),
    description         TEXT,
    product_type        VARCHAR(40) NOT NULL,
    condition           VARCHAR(20) NOT NULL,
    storage_capacity    VARCHAR(40),
    color               VARCHAR(80),
    battery_health      SMALLINT,
    price               NUMERIC(12, 2) NOT NULL,
    previous_price      NUMERIC(12, 2),
    currency            CHAR(3) NOT NULL,
    stock               INT NOT NULL DEFAULT 0,
    warranty            VARCHAR(200),
    imei                VARCHAR(20),
    published           BOOLEAN NOT NULL DEFAULT FALSE,
    featured            BOOLEAN NOT NULL DEFAULT FALSE,
    category_id         UUID,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT chk_products_type CHECK (product_type IN ('IPHONE', 'ACCESSORY')),
    CONSTRAINT chk_products_condition CHECK (condition IN ('NEW', 'USED')),
    CONSTRAINT chk_products_currency CHECK (currency IN ('UYU', 'USD')),
    CONSTRAINT chk_products_stock CHECK (stock >= 0),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_previous_price CHECK (previous_price IS NULL OR previous_price >= 0),
    CONSTRAINT chk_products_battery_health CHECK (
        battery_health IS NULL OR (battery_health >= 0 AND battery_health <= 100)
    ),
    CONSTRAINT chk_products_imei_length CHECK (imei IS NULL OR char_length(imei) BETWEEN 14 AND 17),
    CONSTRAINT uk_products_imei UNIQUE (imei)
);

CREATE INDEX idx_products_published_featured ON products (published, featured);
CREATE INDEX idx_products_type_condition ON products (product_type, condition);
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_products_model ON products (model);

-- Image gallery: URLs/metadata only (no binary storage)
CREATE TABLE product_images (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL,
    url             TEXT NOT NULL,
    public_id       VARCHAR(255),
    alt_text        VARCHAR(255),
    position        INT NOT NULL DEFAULT 0,
    main_image      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_product_images_position CHECK (position >= 0)
);

CREATE INDEX idx_product_images_product_position ON product_images (product_id, position);
-- Una sola imagen principal por producto se refuerza en la capa de aplicación.

-- Variant / attribute features
CREATE TABLE product_features (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL,
    name            VARCHAR(120) NOT NULL,
    feature_value   VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_product_features_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_features_name UNIQUE (product_id, name)
);

CREATE INDEX idx_product_features_product ON product_features (product_id);
