-- Align product_variants.currency with Hibernate / products (VARCHAR, not CHAR).

ALTER TABLE product_variants
    ALTER COLUMN currency TYPE VARCHAR(3) USING currency::VARCHAR(3);
