CREATE TABLE batch (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_code VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    initial_stock INTEGER NOT NULL,
    available_stock INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_batch_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),
    CONSTRAINT uk_batch_product_code
        UNIQUE (product_id, batch_code),
    CONSTRAINT chk_batch_initial_stock_non_negative
        CHECK (initial_stock >= 0),
    CONSTRAINT chk_batch_available_stock
        CHECK (available_stock >= 0),
    CONSTRAINT chk_batch_status
        CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'EXPIRED', 'RETIRED'))
);
