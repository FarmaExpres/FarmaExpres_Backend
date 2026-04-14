const dotenv = require("dotenv");

dotenv.config();

const env = {
  port: Number(process.env.PORT || 8083),
  serviceName: process.env.SERVICE_NAME || "FarmaExpres_Micro_Alert",
  nodeEnv: process.env.NODE_ENV || "development",
  appTimeZone: process.env.APP_TIME_ZONE || "America/Bogota",
  inventory: {
    productsTable: process.env.INVENTORY_PRODUCTS_TABLE || "product",
    expiringSoonDays: Number(process.env.EXPIRING_SOON_DAYS || 15),
  },
  database: {
    host: process.env.DB_HOST || "localhost",
    port: Number(process.env.DB_PORT || 5432),
    name: process.env.DB_NAME || "farmaexpres_inventory",
    user: process.env.DB_USER || "postgres",
    password: process.env.DB_PASSWORD || "1234",
    ssl: String(process.env.DB_SSL || "false").toLowerCase() === "true",
  },
};

module.exports = {
  env,
};
