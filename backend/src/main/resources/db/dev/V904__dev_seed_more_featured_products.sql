-- Development seed (local profile only): more featured iPhones for a richer homepage demo.

UPDATE products SET featured = TRUE WHERE id = '33333333-3333-3333-3333-333333333302';

INSERT INTO products (
    id, slug, name, model, description, product_type, condition,
    storage_capacity, color, battery_health, price, previous_price, currency,
    stock, warranty, imei, published, featured, category_id, created_at, updated_at
) VALUES
(
    '33333333-3333-3333-3333-333333333304',
    'iphone-15-pro-256-titanio-natural-nuevo',
    'iPhone 15 Pro 256GB',
    'iPhone 15 Pro',
    'iPhone 15 Pro nuevo sellado',
    'IPHONE',
    'NEW',
    '256GB',
    'Titanio Natural',
    NULL,
    64990.00,
    NULL,
    'UYU',
    3,
    '1 año Apple',
    NULL,
    TRUE,
    TRUE,
    '22222222-2222-2222-2222-222222222201',
    TIMESTAMP '2026-01-02 12:00:00',
    TIMESTAMP '2026-01-02 12:00:00'
),
(
    '33333333-3333-3333-3333-333333333305',
    'iphone-13-128-azul-usado',
    'iPhone 13 128GB',
    'iPhone 13',
    'iPhone 13 usado en excelente estado',
    'IPHONE',
    'USED',
    '128GB',
    'Azul',
    89,
    22990.00,
    NULL,
    'UYU',
    2,
    '3 meses WA Shop',
    NULL,
    TRUE,
    TRUE,
    '22222222-2222-2222-2222-222222222201',
    TIMESTAMP '2026-01-03 12:00:00',
    TIMESTAMP '2026-01-03 12:00:00'
),
(
    '33333333-3333-3333-3333-333333333306',
    'iphone-12-64-blanco-usado',
    'iPhone 12 64GB',
    'iPhone 12',
    'iPhone 12 usado, muy buen estado',
    'IPHONE',
    'USED',
    '64GB',
    'Blanco',
    87,
    16990.00,
    NULL,
    'UYU',
    1,
    '3 meses WA Shop',
    NULL,
    TRUE,
    TRUE,
    '22222222-2222-2222-2222-222222222201',
    TIMESTAMP '2026-01-04 12:00:00',
    TIMESTAMP '2026-01-04 12:00:00'
);

INSERT INTO product_images (
    id, product_id, url, public_id, alt_text, position, main_image, created_at, updated_at
) VALUES
(
    '66666666-6666-6666-6666-666666666604',
    '33333333-3333-3333-3333-333333333304',
    'assets/marketing/category-nuevos.png',
    NULL,
    'iPhone 15 Pro vista trasera',
    0,
    TRUE,
    TIMESTAMP '2026-01-02 12:00:00',
    TIMESTAMP '2026-01-02 12:00:00'
),
(
    '66666666-6666-6666-6666-666666666605',
    '33333333-3333-3333-3333-333333333305',
    'assets/marketing/category-usados.png',
    NULL,
    'iPhone 13 vista trasera',
    0,
    TRUE,
    TIMESTAMP '2026-01-03 12:00:00',
    TIMESTAMP '2026-01-03 12:00:00'
),
(
    '66666666-6666-6666-6666-666666666606',
    '33333333-3333-3333-3333-333333333306',
    'assets/marketing/hero-iphones.png',
    NULL,
    'iPhone 12 vista trasera',
    0,
    TRUE,
    TIMESTAMP '2026-01-04 12:00:00',
    TIMESTAMP '2026-01-04 12:00:00'
);
