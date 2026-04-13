GRANT USAGE ON SCHEMA public TO farmaexpres_inventory_app;
GRANT USAGE ON SCHEMA public TO farmaexpres_inventory_readonly;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE product TO farmaexpres_inventory_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE batch TO farmaexpres_inventory_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE motion TO farmaexpres_inventory_app;

GRANT USAGE, SELECT ON SEQUENCE product_id_seq TO farmaexpres_inventory_app;
GRANT USAGE, SELECT ON SEQUENCE batch_id_seq TO farmaexpres_inventory_app;
GRANT USAGE, SELECT ON SEQUENCE motion_id_seq TO farmaexpres_inventory_app;

GRANT SELECT ON TABLE product TO farmaexpres_inventory_readonly;
GRANT SELECT ON TABLE batch TO farmaexpres_inventory_readonly;
GRANT SELECT ON TABLE motion TO farmaexpres_inventory_readonly;

GRANT SELECT ON SEQUENCE product_id_seq TO farmaexpres_inventory_readonly;
GRANT SELECT ON SEQUENCE batch_id_seq TO farmaexpres_inventory_readonly;
GRANT SELECT ON SEQUENCE motion_id_seq TO farmaexpres_inventory_readonly;
