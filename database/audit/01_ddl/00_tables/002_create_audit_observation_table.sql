CREATE TABLE audit.audit_observation (
    id BIGSERIAL PRIMARY KEY,
    audit_case_id BIGINT NOT NULL REFERENCES audit.audit_case(id) ON DELETE CASCADE,
    movement_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL,
    created_by_user_id BIGINT,
    created_by_user_name VARCHAR(180),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_audit_observation_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_audit_observation_case_id ON audit.audit_observation(audit_case_id);
CREATE INDEX idx_audit_observation_movement_id ON audit.audit_observation(movement_id);
CREATE INDEX idx_audit_observation_created_at ON audit.audit_observation(created_at DESC);
