REVOKE farmaexpres_auth_app FROM farmaexpres_auth_app_user;
REVOKE farmaexpres_auth_readonly FROM farmaexpres_auth_read_user;

DROP ROLE IF EXISTS farmaexpres_auth_app_user;
DROP ROLE IF EXISTS farmaexpres_auth_read_user;
