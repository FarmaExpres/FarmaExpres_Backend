DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_app_user') THEN
        CREATE ROLE farmaexpres_inventory_app_user
            LOGIN
            PASSWORD 'InventoryApp123!';
    ELSE
        ALTER ROLE farmaexpres_inventory_app_user
            WITH LOGIN
            PASSWORD 'InventoryApp123!';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_read_user') THEN
        CREATE ROLE farmaexpres_inventory_read_user
            LOGIN
            PASSWORD 'InventoryRead123!';
    ELSE
        ALTER ROLE farmaexpres_inventory_read_user
            WITH LOGIN
            PASSWORD 'InventoryRead123!';
    END IF;
END
$$;

GRANT farmaexpres_inventory_app TO farmaexpres_inventory_app_user;
GRANT farmaexpres_inventory_readonly TO farmaexpres_inventory_read_user;
