DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_app') THEN
        CREATE ROLE farmaexpres_inventory_app NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_readonly') THEN
        CREATE ROLE farmaexpres_inventory_readonly NOLOGIN;
    END IF;
END
$$;
