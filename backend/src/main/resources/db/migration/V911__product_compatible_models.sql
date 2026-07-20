-- Which iPhone models an accessory fits (an accessory can fit more than one).
CREATE TABLE product_compatible_models (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    model           VARCHAR(120) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_product_compatible_models_product_id ON product_compatible_models (product_id);
