-- Permanent URL redirects for SEO (slug changes, legacy paths stored by the app).
-- Legacy /catalogo → /iphone is also handled in SeoHtmlDocumentFilter without requiring rows.

CREATE TABLE url_redirects (
    id UUID PRIMARY KEY,
    source_path VARCHAR(500) NOT NULL,
    destination_path VARCHAR(500) NOT NULL,
    status_code INT NOT NULL DEFAULT 301,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_url_redirects_source_path UNIQUE (source_path),
    CONSTRAINT chk_url_redirects_status_code CHECK (status_code IN (301, 302, 307, 308))
);

CREATE INDEX idx_url_redirects_active ON url_redirects (active);
