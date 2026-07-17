CREATE TABLE technical_services (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(220) NOT NULL,
    description         TEXT,
    price               NUMERIC(12, 2),
    currency            CHAR(3),
    estimated_time      VARCHAR(120),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_technical_services_slug UNIQUE (slug),
    CONSTRAINT chk_technical_services_currency CHECK (currency IS NULL OR currency IN ('UYU', 'USD')),
    CONSTRAINT chk_technical_services_price CHECK (price IS NULL OR price >= 0)
);

CREATE INDEX idx_technical_services_active ON technical_services (active);
