-- Users (administrators in v1)
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(40) NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN'))
);

CREATE INDEX idx_users_enabled ON users (enabled);

-- Categories
CREATE TABLE categories (
    id              UUID PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    slug            VARCHAR(140) NOT NULL,
    description     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE INDEX idx_categories_active ON categories (active);
