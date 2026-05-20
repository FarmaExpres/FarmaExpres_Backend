REVOKE SELECT ON SEQUENCE inventory.product_id_seq FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON SEQUENCE inventory.batch_id_seq FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON SEQUENCE inventory.motion_id_seq FROM farmaexpres_inventory_readonly;

REVOKE SELECT ON TABLE inventory.product FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON TABLE inventory.batch FROM farmaexpres_inventory_readonly;
REVOKE SELECT ON TABLE inventory.motion FROM farmaexpres_inventory_readonly;

REVOKE USAGE, SELECT ON SEQUENCE inventory.product_id_seq FROM farmaexpres_inventory_app;
REVOKE USAGE, SELECT ON SEQUENCE inventory.batch_id_seq FROM farmaexpres_inventory_app;
REVOKE USAGE, SELECT ON SEQUENCE inventory.motion_id_seq FROM farmaexpres_inventory_app;

REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.product FROM farmaexpres_inventory_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.batch FROM farmaexpres_inventory_app;
REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.motion FROM farmaexpres_inventory_app;

REVOKE USAGE ON SCHEMA inventory FROM farmaexpres_inventory_readonly;
REVOKE USAGE ON SCHEMA inventory FROM farmaexpres_inventory_app;
