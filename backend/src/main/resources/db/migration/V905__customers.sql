CREATE TABLE customers (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    phone               VARCHAR(40) NOT NULL,
    phone_normalized    VARCHAR(40) NOT NULL,
    email               VARCHAR(320),
    address             TEXT,
    notes               TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_customers_phone_normalized UNIQUE (phone_normalized)
);

CREATE INDEX idx_customers_email ON customers (email);
