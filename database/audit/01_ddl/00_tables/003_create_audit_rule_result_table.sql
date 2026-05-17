CREATE TABLE audit.audit_rule_result (
    id BIGSERIAL PRIMARY KEY,
    audit_case_id BIGINT NOT NULL REFERENCES audit.audit_case(id) ON DELETE CASCADE,
    rule_code VARCHAR(80) NOT NULL,
    rule_name VARCHAR(180) NOT NULL,
    expected_value VARCHAR(120),
    actual_value VARCHAR(120),
    score NUMERIC(5,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_rule_result_case_id ON audit.audit_rule_result(audit_case_id);
CREATE INDEX idx_audit_rule_result_rule_code ON audit.audit_rule_result(rule_code);
