DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_audit_app') THEN
        CREATE ROLE farmaexpres_audit_app NOLOGIN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_audit_readonly') THEN
        CREATE ROLE farmaexpres_audit_readonly NOLOGIN;
    END IF;
END
$$;
