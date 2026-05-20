CREATE TABLE motion (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount INTEGER NOT NULL,
    date_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reason VARCHAR(255),
    user_id BIGINT,
    user_name VARCHAR(150),
    user_email VARCHAR(180),
    user_role VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    marked_by_user_id BIGINT,
    marked_by_user_name VARCHAR(150),
    marked_at TIMESTAMPTZ,
    observation TEXT,
    adjustment_summary TEXT,
    adjustment_detail JSONB,
    produc_id BIGINT,
    batch_id BIGINT,
    CONSTRAINT fk_motion_product
        FOREIGN KEY (produc_id)
        REFERENCES product(id),
    CONSTRAINT fk_motion_batch
        FOREIGN KEY (batch_id)
        REFERENCES batch(id),
    CONSTRAINT chk_motion_type
        CHECK (type IN ('Entrance', 'Updated', 'Exit', 'Deleted')),
    CONSTRAINT chk_motion_status
        CHECK (status IN ('NORMAL', 'MARKED')),
    CONSTRAINT chk_motion_amount_non_negative
        CHECK (amount >= 0)
);
