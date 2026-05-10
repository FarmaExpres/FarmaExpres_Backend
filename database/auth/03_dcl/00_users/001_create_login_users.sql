DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_auth_app_user') THEN
        CREATE ROLE farmaexpres_auth_app_user
            LOGIN
            PASSWORD '${AUTH_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_auth_app_user
            WITH LOGIN
            PASSWORD '${AUTH_DB_PASSWORD}';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_auth_read_user') THEN
        CREATE ROLE farmaexpres_auth_read_user
            LOGIN
            PASSWORD '${AUTH_READ_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_auth_read_user
            WITH LOGIN
            PASSWORD '${AUTH_READ_DB_PASSWORD}';
    END IF;
END
$$;

GRANT farmaexpres_auth_app TO farmaexpres_auth_app_user;
GRANT farmaexpres_auth_readonly TO farmaexpres_auth_read_user;
