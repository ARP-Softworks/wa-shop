-- Align PostgreSQL column types with Hibernate entity mappings (validate mode).
ALTER TABLE products
    ALTER COLUMN currency TYPE VARCHAR(3) USING currency::VARCHAR(3);

ALTER TABLE products
    ALTER COLUMN battery_health TYPE INTEGER USING battery_health::INTEGER;

ALTER TABLE technical_services
    ALTER COLUMN currency TYPE VARCHAR(3) USING currency::VARCHAR(3);
