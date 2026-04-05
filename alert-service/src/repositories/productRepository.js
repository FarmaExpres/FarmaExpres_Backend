const { getPool } = require("../config/database");
const { env } = require("../config/env");

function getCurrentDateExpression() {
  return `(CURRENT_TIMESTAMP AT TIME ZONE '${env.appTimeZone}')::date`;
}

async function findProductsExpiringBetweenDays(minDays, maxDays) {
  const pool = getPool();
  const currentDateExpression = getCurrentDateExpression();
  const query = `
    SELECT
      id,
      code,
      name,
      stock,
      minimumstock AS "minimumStock",
      expirationdate AS "expirationDate",
      asset AS active
    FROM ${env.inventory.productsTable}
    WHERE asset = TRUE
      AND expirationdate >= ${currentDateExpression} + ($1 * INTERVAL '1 day')
      AND expirationdate <= ${currentDateExpression} + ($2 * INTERVAL '1 day')
    ORDER BY expirationdate ASC, name ASC
  `;

  const result = await pool.query(query, [minDays, maxDays]);
  return result.rows;
}

async function findExpiringReportProducts(maxDaysWindow) {
  const pool = getPool();
  const currentDateExpression = getCurrentDateExpression();
  const query = `
    SELECT
      id,
      code,
      name,
      stock,
      minimumstock AS "minimumStock",
      expirationdate AS "expirationDate",
      asset AS active
    FROM ${env.inventory.productsTable}
    WHERE asset = TRUE
      AND expirationdate <= ${currentDateExpression} + ($1 * INTERVAL '1 day')
    ORDER BY expirationdate ASC, name ASC
  `;

  const result = await pool.query(query, [maxDaysWindow]);
  return result.rows;
}

async function findExpiredBatches() {
  const pool = getPool();
  const currentDateExpression = getCurrentDateExpression();
  const query = `
    SELECT
      p.id AS "productId",
      p.code AS "productCode",
      p.name AS "productName",
      p.minimumstock AS "minimumStock",
      b.id AS "batchId",
      b.batch_code AS "batchCode",
      b.expiration_date AS "expirationDate",
      b.available_stock AS "availableStock",
      b.status AS status
    FROM batch b
    JOIN product p ON p.id = b.product_id
    WHERE p.asset = TRUE
      AND b.status <> 'RETIRED'
      AND b.expiration_date < ${currentDateExpression}
    ORDER BY b.expiration_date ASC, p.name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

async function findExpiringBatches(daysWindow, includeExpired = false, onlyWithStock = false) {
  const pool = getPool();
  const currentDateExpression = getCurrentDateExpression();
  const includeExpiredFilter = includeExpired
    ? ""
    : `AND b.expiration_date >= ${currentDateExpression}`;
  const stockFilter = onlyWithStock
    ? "AND b.available_stock > 0"
    : "";
  const query = `
    SELECT
      p.id AS "productId",
      p.code AS "productCode",
      p.name AS "productName",
      p.minimumstock AS "minimumStock",
      b.id AS "batchId",
      b.batch_code AS "batchCode",
      b.expiration_date AS "expirationDate",
      b.available_stock AS "availableStock",
      b.status AS status
    FROM batch b
    JOIN product p ON p.id = b.product_id
    WHERE p.asset = TRUE
      AND b.status <> 'RETIRED'
      ${includeExpiredFilter}
      ${stockFilter}
      AND b.expiration_date <= ${currentDateExpression} + ($1 * INTERVAL '1 day')
    ORDER BY b.expiration_date ASC, p.name ASC
  `;

  const result = await pool.query(query, [daysWindow]);
  return result.rows;
}

async function findLowStockBatches() {
  const pool = getPool();
  const query = `
    SELECT
      p.id AS "productId",
      p.code AS "productCode",
      p.name AS "productName",
      p.minimumstock AS "minimumStock",
      b.id AS "batchId",
      b.batch_code AS "batchCode",
      b.expiration_date AS "expirationDate",
      b.available_stock AS "availableStock",
      b.status AS status
    FROM batch b
    JOIN product p ON p.id = b.product_id
    WHERE p.asset = TRUE
      AND b.status <> 'RETIRED'
      AND b.available_stock > 0
      AND b.available_stock <= p.minimumstock
    ORDER BY b.available_stock ASC, p.name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

async function findOutOfStockBatches() {
  const pool = getPool();
  const query = `
    SELECT
      p.id AS "productId",
      p.code AS "productCode",
      p.name AS "productName",
      p.minimumstock AS "minimumStock",
      b.id AS "batchId",
      b.batch_code AS "batchCode",
      b.expiration_date AS "expirationDate",
      b.available_stock AS "availableStock",
      b.status AS status
    FROM batch b
    JOIN product p ON p.id = b.product_id
    WHERE p.asset = TRUE
      AND b.status <> 'RETIRED'
      AND b.available_stock = 0
    ORDER BY b.expiration_date ASC, p.name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

module.exports = {
  findExpiredBatches,
  findExpiringBatches,
  findLowStockBatches,
  findOutOfStockBatches,
  findExpiringReportProducts,
  findProductsExpiringBetweenDays,
};
