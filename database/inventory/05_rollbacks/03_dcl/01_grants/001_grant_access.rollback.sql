REVOKE SELECT ON SEQUENCE product_id_seq FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON SEQUENCE batch_id_seq FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON SEQUENCE motion_id_seq FROM farmaexpres_inventory_readonly;

REVOKE SELECT ON TABLE product FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON TABLE batch FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON TABLE motion FROM farmaexpres_inventory_readonly;

REVOKE USAGE, SELECT ON SEQUENCE product_id_seq FROM farmaexpres_inventory_app;
REVOKE USAGE, SELECT ON SEQUENCE batch_id_seq FROM farmaexpres_inventory_app;
REVOKE USAGE, SELECT ON SEQUENCE motion_id_seq FROM farmaexpres_inventory_app;

REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE product FROM farmaexpres_inventory_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE batch FROM farmaexpres_inventory_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE motion FROM farmaexpres_inventory_app;

REVOKE USAGE ON SCHEMA public FROM farmaexpres_inventory_readonly;
REVOKE USAGE ON SCHEMA public FROM farmaexpres_inventory_app;
