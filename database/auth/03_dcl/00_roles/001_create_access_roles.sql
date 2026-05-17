DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_auth_app') THEN
        CREATE ROLE farmaexpres_auth_app NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_auth_readonly') THEN
        CREATE ROLE farmaexpres_auth_readonly NOLOGIN;
    END IF;
END
$$;
