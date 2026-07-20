-- Groups variant products (same model, different color/capacity) so the public
-- product page can show a picker of the combinations that actually exist.
CREATE TABLE product_groups (
    id              UUID PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(220) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_groups_slug UNIQUE (slug)
);

ALTER TABLE products ADD COLUMN product_group_id UUID REFERENCES product_groups(id) ON DELETE SET NULL;

CREATE INDEX idx_products_product_group_id ON products (product_group_id);
