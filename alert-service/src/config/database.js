const { Pool } = require("pg");
const { env } = require("./env");

let pool;

function getPool() {
  if (!pool) {
    pool = new Pool({
      host: env.database.host,
      port: env.database.port,
      database: env.database.name,
      user: env.database.user,
      password: env.database.password,
      ssl: env.database.ssl ? { rejectUnauthorized: false } : false,
    });
  }

  return pool;
}

module.exports = {
  getPool,
};
