-- Single-row site configuration (application enforces singleton usage)
CREATE TABLE site_settings (
    id                  UUID PRIMARY KEY,
    business_name       VARCHAR(200) NOT NULL,
    whatsapp_number     VARCHAR(40),
    instagram_url       VARCHAR(500),
    address             VARCHAR(500),
    opening_hours       VARCHAR(500),
    contact_email       VARCHAR(320),
    logo_url            TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL
);
