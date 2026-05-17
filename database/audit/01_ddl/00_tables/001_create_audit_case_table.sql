CREATE TABLE audit.audit_case (
    id BIGSERIAL PRIMARY KEY,
    movement_id BIGINT NOT NULL,
    product_id BIGINT,
    batch_id BIGINT,
    movement_type VARCHAR(30) NOT NULL,
    medicine_name VARCHAR(180),
    quantity INTEGER NOT NULL,
    movement_reason VARCHAR(255),
    movement_user_id BIGINT,
    movement_user_name VARCHAR(180),
    movement_user_role VARCHAR(80),
    movement_date_time TIMESTAMP WITH TIME ZONE,
    risk_score NUMERIC(5,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    created_by_user_id BIGINT,
    created_by_user_name VARCHAR(180),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    closed_by_user_id BIGINT,
    closed_by_user_name VARCHAR(180),
    closed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_audit_case_status CHECK (status IN ('OPEN', 'IN_REVIEW', 'REVIEWED', 'CLOSED')),
    CONSTRAINT chk_audit_case_source CHECK (source IN ('AUTO', 'MANUAL')),
    CONSTRAINT chk_audit_case_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_audit_case_movement_id ON audit.audit_case(movement_id);
CREATE INDEX idx_audit_case_status ON audit.audit_case(status);
CREATE INDEX idx_audit_case_source ON audit.audit_case(source);
CREATE INDEX idx_audit_case_priority ON audit.audit_case(priority);
CREATE INDEX idx_audit_case_created_at ON audit.audit_case(created_at DESC);

CREATE UNIQUE INDEX uq_audit_case_open_movement
ON audit.audit_case(movement_id)
WHERE status IN ('OPEN', 'IN_REVIEW');
