REVOKE SELECT ON SEQUENCE role_id_role_seq FROM farmaexpres_auth_readonly;
REVOKE SELECT ON SEQUENCE users_id_seq FROM farmaexpres_auth_readonly;
REVOKE SELECT ON SEQUENCE binnacle_id_seq FROM farmaexpres_auth_readonly;

REVOKE SELECT ON TABLE role FROM farmaexpres_auth_readonly;
REVOKE SELECT ON TABLE users FROM farmaexpres_auth_readonly;
REVOKE SELECT ON TABLE binnacle FROM farmaexpres_auth_readonly;

REVOKE USAGE, SELECT ON SEQUENCE role_id_role_seq FROM farmaexpres_auth_app;
REVOKE USAGE, SELECT ON SEQUENCE users_id_seq FROM farmaexpres_auth_app;
REVOKE USAGE, SELECT ON SEQUENCE binnacle_id_seq FROM farmaexpres_auth_app;

REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE role FROM farmaexpres_auth_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE users FROM farmaexpres_auth_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE binnacle FROM farmaexpres_auth_app;

REVOKE USAGE ON SCHEMA public FROM farmaexpres_auth_readonly;
REVOKE USAGE ON SCHEMA public FROM farmaexpres_auth_app;
