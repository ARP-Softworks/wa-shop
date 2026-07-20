-- Cross-category "buy X from group A, get Y from group B at Z% off" promotions.
-- Same trigger/reward category covers mix-and-match (e.g. any 3 cases, cheapest free).
CREATE TABLE promotions (
    id                   UUID PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    active               BOOLEAN NOT NULL DEFAULT TRUE,
    trigger_category_id  UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    trigger_quantity     INTEGER NOT NULL,
    reward_category_id   UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    reward_quantity      INTEGER NOT NULL,
    discount_percent     INTEGER NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_promotions_trigger_qty CHECK (trigger_quantity >= 1),
    CONSTRAINT chk_promotions_reward_qty CHECK (reward_quantity >= 1),
    CONSTRAINT chk_promotions_discount CHECK (discount_percent BETWEEN 1 AND 100)
);

CREATE INDEX idx_promotions_active ON promotions (active);
