INSERT INTO motion (type, amount, reason, user_name, user_email, user_role, status, produc_id, batch_id)
SELECT
    'Entrance',
    p.stock,
    'Carga inicial de catalogo',
    'SYSTEM_INIT',
    'system@farmaexpres.local',
    'SYSTEM',
    'NORMAL',
    p.id,
    b.id
FROM product p
JOIN batch b
  ON b.product_id = p.id
 AND b.batch_code = CONCAT('INIT-', p.code);
