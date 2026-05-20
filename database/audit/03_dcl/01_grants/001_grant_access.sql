GRANT USAGE ON SCHEMA audit TO farmaexpres_audit_app;
GRANT USAGE ON SCHEMA audit TO farmaexpres_audit_readonly;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE audit.audit_case TO farmaexpres_audit_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE audit.audit_observation TO farmaexpres_audit_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE audit.audit_rule_result TO farmaexpres_audit_app;

GRANT USAGE, SELECT ON SEQUENCE audit.audit_case_id_seq TO farmaexpres_audit_app;
GRANT USAGE, SELECT ON SEQUENCE audit.audit_observation_id_seq TO farmaexpres_audit_app;
GRANT USAGE, SELECT ON SEQUENCE audit.audit_rule_result_id_seq TO farmaexpres_audit_app;

GRANT SELECT ON TABLE audit.audit_case TO farmaexpres_audit_readonly;
GRANT SELECT ON TABLE audit.audit_observation TO farmaexpres_audit_readonly;
GRANT SELECT ON TABLE audit.audit_rule_result TO farmaexpres_audit_readonly;

GRANT SELECT ON SEQUENCE audit.audit_case_id_seq TO farmaexpres_audit_readonly;
GRANT SELECT ON SEQUENCE audit.audit_observation_id_seq TO farmaexpres_audit_readonly;
GRANT SELECT ON SEQUENCE audit.audit_rule_result_id_seq TO farmaexpres_audit_readonly;
