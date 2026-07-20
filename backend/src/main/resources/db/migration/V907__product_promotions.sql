-- INTEGER (not SMALLINT) to match Hibernate's default column type for a Java Integer field
-- (see V901, which had to fix the same battery_health mismatch).
ALTER TABLE products
    ADD COLUMN promo_buy_quantity INTEGER;

ALTER TABLE products
    ADD COLUMN promo_pay_quantity INTEGER;

ALTER TABLE products
    ADD CONSTRAINT chk_products_promo CHECK (
        (promo_buy_quantity IS NULL AND promo_pay_quantity IS NULL)
        OR (promo_buy_quantity IS NOT NULL AND promo_pay_quantity IS NOT NULL
            AND promo_pay_quantity >= 1 AND promo_buy_quantity > promo_pay_quantity)
    );
