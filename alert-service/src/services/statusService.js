const { env } = require("../config/env");
const HealthStatus = require("../models/HealthStatus");
const { getCurrentTimestamp } = require("../utils/dateUtils");
const healthRepository = require("../repositories/healthRepository");

async function buildStatus() {
  const databaseReachable = await healthRepository.isDatabaseReachable();
  const status = databaseReachable ? "UP" : "DOWN";

  return new HealthStatus({
    status,
    service: env.serviceName,
    timestamp: getCurrentTimestamp(),
    database: databaseReachable ? "UP" : "DOWN",
  });
}

module.exports = {
  buildStatus,
};
