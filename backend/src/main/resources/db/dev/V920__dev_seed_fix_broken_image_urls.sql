-- V900 seeded these two with placeholder URLs on Cloudinary's public "demo" account, which
-- don't actually exist there (404) — broken-image alt text was bleeding through the card
-- badges on the frontend. Point them at the same local marketing assets already used by other
-- seed products (see iPhone 13 / iPhone 15 Pro) instead of a dead external host.
UPDATE product_images
SET url = 'assets/marketing/category-servicio.png'
WHERE id = '66666666-6666-6666-6666-666666666601';

UPDATE product_images
SET url = 'assets/marketing/category-accesorios.png'
WHERE id = '66666666-6666-6666-6666-666666666602';
