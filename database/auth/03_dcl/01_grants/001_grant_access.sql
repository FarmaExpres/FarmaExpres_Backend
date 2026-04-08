GRANT USAGE ON SCHEMA public TO farmaexpres_auth_app;
GRANT USAGE ON SCHEMA public TO farmaexpres_auth_readonly;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE role TO farmaexpres_auth_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE users TO farmaexpres_auth_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE binnacle TO farmaexpres_auth_app;

GRANT USAGE, SELECT ON SEQUENCE role_id_role_seq TO farmaexpres_auth_app;
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO farmaexpres_auth_app;
GRANT USAGE, SELECT ON SEQUENCE binnacle_id_seq TO farmaexpres_auth_app;

GRANT SELECT ON TABLE role TO farmaexpres_auth_readonly;
GRANT SELECT ON TABLE users TO farmaexpres_auth_readonly;
GRANT SELECT ON TABLE binnacle TO farmaexpres_auth_readonly;

GRANT SELECT ON SEQUENCE role_id_role_seq TO farmaexpres_auth_readonly;
GRANT SELECT ON SEQUENCE users_id_seq TO farmaexpres_auth_readonly;
GRANT SELECT ON SEQUENCE binnacle_id_seq TO farmaexpres_auth_readonly;
