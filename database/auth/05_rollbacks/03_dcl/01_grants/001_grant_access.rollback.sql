REVOKE SELECT ON SEQUENCE auth.role_id_role_seq FROM farmaexpres_auth_readonly;
REVOKE SELECT ON SEQUENCE auth.users_id_seq FROM farmaexpres_auth_readonly;
REVOKE SELECT ON SEQUENCE auth.binnacle_id_seq FROM farmaexpres_auth_readonly;
REVOKE SELECT ON SEQUENCE auth.refresh_token_id_seq FROM farmaexpres_auth_readonly;

REVOKE SELECT ON TABLE auth.role FROM farmaexpres_auth_readonly;
REVOKE SELECT ON TABLE auth.users FROM farmaexpres_auth_readonly;
REVOKE SELECT ON TABLE auth.binnacle FROM farmaexpres_auth_readonly;
REVOKE SELECT ON TABLE auth.refresh_token FROM farmaexpres_auth_readonly;

REVOKE USAGE, SELECT ON SEQUENCE auth.role_id_role_seq FROM farmaexpres_auth_app;
REVOKE USAGE, SELECT ON SEQUENCE auth.users_id_seq FROM farmaexpres_auth_app;
REVOKE USAGE, SELECT ON SEQUENCE auth.binnacle_id_seq FROM farmaexpres_auth_app;
REVOKE USAGE, SELECT ON SEQUENCE auth.refresh_token_id_seq FROM farmaexpres_auth_app;

REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE auth.role FROM farmaexpres_auth_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE auth.users FROM farmaexpres_auth_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE auth.binnacle FROM farmaexpres_auth_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE auth.refresh_token FROM farmaexpres_auth_app;

REVOKE USAGE ON SCHEMA auth FROM farmaexpres_auth_readonly;
REVOKE USAGE ON SCHEMA auth FROM farmaexpres_auth_app;
