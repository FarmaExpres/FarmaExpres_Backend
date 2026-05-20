const dotenv = require("dotenv");

dotenv.config();

const env = {
  port: Number(process.env.PORT || 8083),
  serviceName: process.env.SERVICE_NAME || "FarmaExpres_Micro_Alert",
  nodeEnv: process.env.NODE_ENV || "development",
  appTimeZone: process.env.APP_TIME_ZONE || "America/Bogota",
  inventory: {
    expiringSoonDays: Number(process.env.EXPIRING_SOON_DAYS || 15),
    serviceUrl: process.env.INVENTORY_SERVICE_URL || "http://localhost:8082",
    requestTimeoutMs: Number(process.env.INVENTORY_SERVICE_TIMEOUT_MS || 5000),
  },
};

module.exports = {
  env,
};
