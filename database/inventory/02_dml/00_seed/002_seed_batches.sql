INSERT INTO batch (product_id, batch_code, expiration_date, initial_stock, available_stock, status)
SELECT
    id,
    CONCAT('INIT-', code),
    expirationdate,
    stock,
    stock,
    CASE
        WHEN expirationdate < CURRENT_DATE THEN 'EXPIRED'
        WHEN stock = 0 THEN 'OUT_OF_STOCK'
        ELSE 'ACTIVE'
    END
FROM product;
