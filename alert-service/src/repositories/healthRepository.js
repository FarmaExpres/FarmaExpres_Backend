const { getPool } = require("../config/database");

async function isDatabaseReachable() {
  try {
    const pool = getPool();
    await pool.query("SELECT 1");
    return true;
  } catch (_error) {
    return false;
  }
}

module.exports = {
  isDatabaseReachable,
};
