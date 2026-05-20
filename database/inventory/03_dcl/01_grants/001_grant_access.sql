GRANT USAGE ON SCHEMA inventory TO farmaexpres_inventory_app;
GRANT USAGE ON SCHEMA inventory TO farmaexpres_inventory_readonly;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.product TO farmaexpres_inventory_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.batch TO farmaexpres_inventory_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE inventory.motion TO farmaexpres_inventory_app;

GRANT USAGE, SELECT ON SEQUENCE inventory.product_id_seq TO farmaexpres_inventory_app;
GRANT USAGE, SELECT ON SEQUENCE inventory.batch_id_seq TO farmaexpres_inventory_app;
GRANT USAGE, SELECT ON SEQUENCE inventory.motion_id_seq TO farmaexpres_inventory_app;

GRANT SELECT ON TABLE inventory.product TO farmaexpres_inventory_readonly;
GRANT SELECT ON TABLE inventory.batch TO farmaexpres_inventory_readonly;
GRANT SELECT ON TABLE inventory.motion TO farmaexpres_inventory_readonly;

GRANT SELECT ON SEQUENCE inventory.product_id_seq TO farmaexpres_inventory_readonly;
GRANT SELECT ON SEQUENCE inventory.batch_id_seq TO farmaexpres_inventory_readonly;
GRANT SELECT ON SEQUENCE inventory.motion_id_seq TO farmaexpres_inventory_readonly;
