DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_audit_app_user') THEN
        CREATE ROLE farmaexpres_audit_app_user
            LOGIN
            PASSWORD '${AUDIT_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_audit_app_user
            WITH LOGIN
            PASSWORD '${AUDIT_DB_PASSWORD}';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmaexpres_audit_read_user') THEN
        CREATE ROLE farmaexpres_audit_read_user
            LOGIN
            PASSWORD '${AUDIT_READ_DB_PASSWORD}';
    ELSE
        ALTER ROLE farmaexpres_audit_read_user
            WITH LOGIN
            PASSWORD '${AUDIT_READ_DB_PASSWORD}';
    END IF;
END
$$;

GRANT farmaexpres_audit_app TO farmaexpres_audit_app_user;
GRANT farmaexpres_audit_readonly TO farmaexpres_audit_read_user;
