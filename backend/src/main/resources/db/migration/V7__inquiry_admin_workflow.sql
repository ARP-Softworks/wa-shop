-- Extend inquiries for admin workflow
ALTER TABLE inquiries DROP CONSTRAINT IF EXISTS chk_inquiries_status;
ALTER TABLE inquiries ADD CONSTRAINT chk_inquiries_status
    CHECK (status IN ('NEW', 'IN_PROGRESS', 'RESPONDED', 'CLOSED'));

ALTER TABLE inquiries ADD COLUMN IF NOT EXISTS admin_notes TEXT;

CREATE TABLE inquiry_status_history (
    id              UUID PRIMARY KEY,
    inquiry_id      UUID NOT NULL,
    from_status     VARCHAR(40),
    to_status       VARCHAR(40) NOT NULL,
    changed_by      UUID,
    note            TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_inquiry_status_history_inquiry
        FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_inquiry_status_history_user
        FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_inquiry_status_history_to
        CHECK (to_status IN ('NEW', 'IN_PROGRESS', 'RESPONDED', 'CLOSED'))
);

CREATE INDEX idx_inquiry_status_history_inquiry ON inquiry_status_history (inquiry_id, created_at DESC);
