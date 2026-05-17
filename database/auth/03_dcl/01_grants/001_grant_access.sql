GRANT USAGE ON SCHEMA auth TO farmaexpres_auth_app;
GRANT USAGE ON SCHEMA auth TO farmaexpres_auth_readonly;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE auth.role TO farmaexpres_auth_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE auth.users TO farmaexpres_auth_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE auth.binnacle TO farmaexpres_auth_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE auth.refresh_token TO farmaexpres_auth_app;

GRANT USAGE, SELECT ON SEQUENCE auth.role_id_role_seq TO farmaexpres_auth_app;
GRANT USAGE, SELECT ON SEQUENCE auth.users_id_seq TO farmaexpres_auth_app;
GRANT USAGE, SELECT ON SEQUENCE auth.binnacle_id_seq TO farmaexpres_auth_app;
GRANT USAGE, SELECT ON SEQUENCE auth.refresh_token_id_seq TO farmaexpres_auth_app;

GRANT SELECT ON TABLE auth.role TO farmaexpres_auth_readonly;
GRANT SELECT ON TABLE auth.users TO farmaexpres_auth_readonly;
GRANT SELECT ON TABLE auth.binnacle TO farmaexpres_auth_readonly;
GRANT SELECT ON TABLE auth.refresh_token TO farmaexpres_auth_readonly;

GRANT SELECT ON SEQUENCE auth.role_id_role_seq TO farmaexpres_auth_readonly;
GRANT SELECT ON SEQUENCE auth.users_id_seq TO farmaexpres_auth_readonly;
GRANT SELECT ON SEQUENCE auth.binnacle_id_seq TO farmaexpres_auth_readonly;
GRANT SELECT ON SEQUENCE auth.refresh_token_id_seq TO farmaexpres_auth_readonly;
