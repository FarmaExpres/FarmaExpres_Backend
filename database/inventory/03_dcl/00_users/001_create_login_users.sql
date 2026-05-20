DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_app_user') THEN
        CREATE ROLE farmaexpres_inventory_app_user
            LOGIN
            PASSWORD '${INVENTORY_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_inventory_app_user
            WITH LOGIN
            PASSWORD '${INVENTORY_DB_PASSWORD}';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_inventory_read_user') THEN
        CREATE ROLE farmaexpres_inventory_read_user
            LOGIN
            PASSWORD '${INVENTORY_READ_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_inventory_read_user
            WITH LOGIN
            PASSWORD '${INVENTORY_READ_DB_PASSWORD}';
    END IF;
END
$$;

GRANT farmaexpres_inventory_app TO farmaexpres_inventory_app_user;
GRANT farmaexpres_inventory_readonly TO farmaexpres_inventory_read_user;
