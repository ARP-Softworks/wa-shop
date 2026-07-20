-- Discount / coupon codes for checkout.
CREATE TABLE discount_codes (
    id               UUID PRIMARY KEY,
    code             VARCHAR(40) NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    discount_type    VARCHAR(20) NOT NULL,
    discount_value   NUMERIC(12, 2) NOT NULL,
    max_uses         INTEGER,
    used_count       INTEGER NOT NULL DEFAULT 0,
    starts_at        TIMESTAMP WITH TIME ZONE,
    ends_at          TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_discount_codes_code UNIQUE (code),
    CONSTRAINT chk_discount_codes_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT chk_discount_codes_value CHECK (discount_value > 0),
    CONSTRAINT chk_discount_codes_max_uses CHECK (max_uses IS NULL OR max_uses >= 1),
    CONSTRAINT chk_discount_codes_used_count CHECK (used_count >= 0),
    CONSTRAINT chk_discount_codes_window CHECK (starts_at IS NULL OR ends_at IS NULL OR starts_at <= ends_at)
);

CREATE INDEX idx_discount_codes_active ON discount_codes (active);

CREATE TABLE discount_code_redemptions (
    id                 UUID PRIMARY KEY,
    discount_code_id   UUID NOT NULL REFERENCES discount_codes(id) ON DELETE CASCADE,
    order_id           UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    phone_normalized   VARCHAR(40) NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_discount_redemption_order UNIQUE (order_id)
);

CREATE INDEX idx_discount_redemptions_code_phone ON discount_code_redemptions (discount_code_id, phone_normalized);

ALTER TABLE orders
    ADD COLUMN discount_code_id UUID REFERENCES discount_codes(id) ON DELETE SET NULL,
    ADD COLUMN discount_code VARCHAR(40),
    ADD COLUMN coupon_discount NUMERIC(12, 2) NOT NULL DEFAULT 0.00;
