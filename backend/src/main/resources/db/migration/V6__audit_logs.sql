CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY,
    user_id         UUID,
    action          VARCHAR(40) NOT NULL,
    entity_type     VARCHAR(80) NOT NULL,
    entity_id       VARCHAR(64),
    details         TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_audit_logs_action CHECK (
        action IN ('CREATE', 'UPDATE', 'DELETE', 'PUBLISH', 'UNPUBLISH', 'LOGIN', 'OTHER')
    )
);

CREATE INDEX idx_audit_logs_created ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
