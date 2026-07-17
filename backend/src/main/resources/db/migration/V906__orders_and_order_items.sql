CREATE TABLE orders (
    id                  UUID PRIMARY KEY,
    customer_id         UUID NOT NULL,
    status              VARCHAR(20) NOT NULL,
    subtotal            NUMERIC(12, 2) NOT NULL,
    total                NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    shipping_address    TEXT,
    mp_preference_id    VARCHAR(100),
    mp_payment_id       VARCHAR(100),
    mp_payment_status   VARCHAR(40),
    notes               TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_status CHECK (
        status IN ('PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REJECTED', 'EXPIRED')
    ),
    CONSTRAINT chk_orders_currency CHECK (currency IN ('UYU', 'USD')),
    CONSTRAINT chk_orders_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_orders_total CHECK (total >= 0)
);

CREATE INDEX idx_orders_status_created ON orders (status, created_at DESC);
CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_mp_preference ON orders (mp_preference_id);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    product_id      UUID,
    product_name    VARCHAR(200) NOT NULL,
    unit_price      NUMERIC(12, 2) NOT NULL,
    quantity        INT NOT NULL,
    subtotal        NUMERIC(12, 2) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal >= 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

CREATE TABLE order_status_history (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    from_status     VARCHAR(20),
    to_status       VARCHAR(20) NOT NULL,
    changed_by      UUID,
    note            TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_order_status_history_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_history_user
        FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_order_status_history_to CHECK (
        to_status IN ('PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REJECTED', 'EXPIRED')
    )
);

CREATE INDEX idx_order_status_history_order ON order_status_history (order_id, created_at DESC);
