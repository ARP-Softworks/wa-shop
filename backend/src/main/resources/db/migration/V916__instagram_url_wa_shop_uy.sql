-- Real Instagram profile for WA Shop
UPDATE site_settings
SET instagram_url = 'https://www.instagram.com/wa.shop.uy/',
    updated_at = NOW()
WHERE instagram_url IS NULL
   OR instagram_url = ''
   OR instagram_url IN (
        'https://instagram.com/washop',
        'https://www.instagram.com/washop',
        'https://instagram.com/washop.uy',
        'https://www.instagram.com/washop.uy'
   );
