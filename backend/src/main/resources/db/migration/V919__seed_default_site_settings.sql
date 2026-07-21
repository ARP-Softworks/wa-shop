-- AdminSettingsService.requireSettings() always looks up SiteSettings.DEFAULT_ID and 404s if
-- missing. The only place that row was ever inserted was db/dev's demo seed migration, which
-- never runs outside the "local" profile — so any real deployment (dev/production on Render)
-- starts with an empty site_settings table and the admin "Configuración" page 404s forever.
-- Guarded by WHERE NOT EXISTS so it's a no-op wherever a row already exists (local, or a prod
-- environment where the admin already saved settings).
INSERT INTO site_settings (
    id, business_name, whatsapp_number, instagram_url, address, opening_hours,
    contact_email, logo_url, created_at, updated_at
)
SELECT
    '55555555-5555-5555-5555-555555555555',
    'WA Shop',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM site_settings);
