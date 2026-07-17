CREATE TABLE inquiries (
    id              UUID PRIMARY KEY,
    customer_name   VARCHAR(200) NOT NULL,
    phone           VARCHAR(40),
    email           VARCHAR(320),
    message         TEXT NOT NULL,
    product_id      UUID,
    status          VARCHAR(40) NOT NULL,
    source          VARCHAR(40) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_inquiries_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT chk_inquiries_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'CLOSED')),
    CONSTRAINT chk_inquiries_source CHECK (source IN ('WHATSAPP_CLICK', 'FORM', 'OTHER'))
);

CREATE INDEX idx_inquiries_status_created ON inquiries (status, created_at DESC);
CREATE INDEX idx_inquiries_product ON inquiries (product_id);
