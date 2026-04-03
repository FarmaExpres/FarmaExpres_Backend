const { getPool } = require("../config/database");
const { env } = require("../config/env");

async function findLowStockProducts() {
  const pool = getPool();
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
      AND stock < minimumstock
    ORDER BY stock ASC, name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

async function findExpiredProducts() {
  const pool = getPool();
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
      AND expirationdate < CURRENT_DATE
    ORDER BY expirationdate ASC, name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

async function findOutOfStockProducts() {
  const pool = getPool();
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
      AND stock = 0
    ORDER BY name ASC
  `;

  const result = await pool.query(query);
  return result.rows;
}

async function findExpiringSoonProducts(daysWindow) {
  const pool = getPool();
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
      AND expirationdate > CURRENT_DATE
      AND expirationdate <= CURRENT_DATE + ($1 * INTERVAL '1 day')
    ORDER BY expirationdate ASC, name ASC
  `;

  const result = await pool.query(query, [daysWindow]);
  return result.rows;
}

async function findProductsExpiringBetweenDays(minDays, maxDays) {
  const pool = getPool();
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
      AND expirationdate >= CURRENT_DATE + ($1 * INTERVAL '1 day')
      AND expirationdate <= CURRENT_DATE + ($2 * INTERVAL '1 day')
    ORDER BY expirationdate ASC, name ASC
  `;

  const result = await pool.query(query, [minDays, maxDays]);
  return result.rows;
}

module.exports = {
  findExpiringSoonProducts,
  findProductsExpiringBetweenDays,
  findExpiredProducts,
  findLowStockProducts,
  findOutOfStockProducts,
};
