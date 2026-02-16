-- Ispravljena SQL komanda za bazu
-- Ažuriranje any NULL vrednosti u logged_in koloni
UPDATE driver SET logged_in = false WHERE logged_in IS NULL;

-- Provera da li je kolona успешно azurirana
SELECT id, first_name, last_name, logged_in, is_active FROM driver LIMIT 5;

-- Provera tabele driver_status_event
SELECT COUNT(*) as event_count FROM driver_status_event;

