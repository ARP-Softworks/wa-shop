-- Rotating promotional images shown on the home page hero, each optionally
-- clickable to an internal or external link.
CREATE TABLE hero_banners (
    id              UUID PRIMARY KEY,
    image_url       TEXT NOT NULL,
    image_public_id VARCHAR(300),
    alt_text        VARCHAR(255),
    link_url        VARCHAR(500),
    position        INTEGER NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_hero_banners_active_position ON hero_banners (active, position);
