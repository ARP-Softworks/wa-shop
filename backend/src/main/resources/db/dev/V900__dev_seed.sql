-- Development seed (local profile only). Password: ChangeMe123! (BCrypt)

INSERT INTO users (
    id, email, password_hash, first_name, last_name, role, enabled, created_at, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin@washop.uy',
    '$2a$10$v0bb7jPANI3m22Pwk2EZfO.nT4JU1NGLDhNFtUeagxE4TJFcDZq6.',
    'Admin',
    'WA Shop',
    'ADMIN',
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO categories (id, name, slug, description, active, created_at, updated_at) VALUES
(
    '22222222-2222-2222-2222-222222222201',
    'iPhone',
    'iphone',
    'iPhone nuevos y usados',
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
),
(
    '22222222-2222-2222-2222-222222222202',
    'Accesorios',
    'accesorios',
    'Fundas, cargadores y más',
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO products (
    id, slug, name, model, description, product_type, condition,
    storage_capacity, color, battery_health, price, previous_price, currency,
    stock, warranty, imei, published, featured, category_id, created_at, updated_at
) VALUES
(
    '33333333-3333-3333-3333-333333333301',
    'iphone-15-128-black-nuevo',
    'iPhone 15 128GB',
    'iPhone 15',
    'iPhone 15 nuevo sellado',
    'IPHONE',
    'NEW',
    '128GB',
    'Black',
    NULL,
    42990.00,
    NULL,
    'UYU',
    2,
    '1 año Apple',
    NULL,
    TRUE,
    TRUE,
    '22222222-2222-2222-2222-222222222201',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
),
(
    '33333333-3333-3333-3333-333333333302',
    'iphone-14-128-midnight-usado',
    'iPhone 14 128GB',
    'iPhone 14',
    'iPhone 14 usado en excelente estado',
    'IPHONE',
    'USED',
    '128GB',
    'Midnight',
    92,
    28990.00,
    31990.00,
    'UYU',
    1,
    '3 meses WA Shop',
    '356938035643809',
    TRUE,
    FALSE,
    '22222222-2222-2222-2222-222222222201',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
),
(
    '33333333-3333-3333-3333-333333333303',
    'funda-silicona-iphone-15',
    'Funda silicona iPhone 15',
    NULL,
    'Funda de silicona compatible con iPhone 15',
    'ACCESSORY',
    'NEW',
    NULL,
    'Azul',
    NULL,
    1290.00,
    NULL,
    'UYU',
    10,
    NULL,
    NULL,
    TRUE,
    FALSE,
    '22222222-2222-2222-2222-222222222202',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO product_images (
    id, product_id, url, public_id, alt_text, position, main_image, created_at, updated_at
) VALUES
(
    '66666666-6666-6666-6666-666666666601',
    '33333333-3333-3333-3333-333333333301',
    'https://res.cloudinary.com/demo/image/upload/iphone15-main.jpg',
    'washop/iphone15-main',
    'iPhone 15 vista frontal',
    0,
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
),
(
    '66666666-6666-6666-6666-666666666602',
    '33333333-3333-3333-3333-333333333302',
    'https://res.cloudinary.com/demo/image/upload/iphone14-main.jpg',
    'washop/iphone14-main',
    'iPhone 14 vista frontal',
    0,
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO product_features (id, product_id, name, feature_value, created_at, updated_at) VALUES
(
    '77777777-7777-7777-7777-777777777701',
    '33333333-3333-3333-3333-333333333302',
    'Estado cosmético',
    'Excelente',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
),
(
    '77777777-7777-7777-7777-777777777702',
    '33333333-3333-3333-3333-333333333303',
    'Compatibilidad',
    'iPhone 15 / 15 Pro',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO technical_services (
    id, name, slug, description, price, currency, estimated_time, active, created_at, updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444401',
    'Cambio de pantalla',
    'cambio-de-pantalla',
    'Reemplazo de pantalla original o compatible según modelo',
    4500.00,
    'UYU',
    '24-48 hs',
    TRUE,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO site_settings (
    id, business_name, whatsapp_number, instagram_url, address, opening_hours,
    contact_email, logo_url, created_at, updated_at
) VALUES (
    '55555555-5555-5555-5555-555555555555',
    'WA Shop',
    '59890000000',
    'https://instagram.com/washop',
    'Montevideo, Uruguay',
    'Lun a Vie 10:00-19:00',
    'hola@washop.uy',
    NULL,
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);

INSERT INTO inquiries (
    id, customer_name, phone, email, message, product_id, status, source, created_at, updated_at
) VALUES (
    '88888888-8888-8888-8888-888888888801',
    'Cliente Demo',
    '59891111111',
    'cliente@example.com',
    'Consulta de prueba sobre iPhone 14',
    '33333333-3333-3333-3333-333333333302',
    'NEW',
    'WHATSAPP_CLICK',
    TIMESTAMP '2026-01-01 12:00:00',
    TIMESTAMP '2026-01-01 12:00:00'
);
