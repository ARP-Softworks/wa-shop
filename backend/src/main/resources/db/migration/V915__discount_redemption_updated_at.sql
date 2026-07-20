-- BaseEntity requires updated_at on discount_code_redemptions.
ALTER TABLE discount_code_redemptions
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

ALTER TABLE discount_code_redemptions
    ALTER COLUMN updated_at DROP DEFAULT;
