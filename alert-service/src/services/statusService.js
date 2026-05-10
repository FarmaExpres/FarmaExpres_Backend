const { env } = require("../config/env");
const HealthStatus = require("../models/HealthStatus");
const { getCurrentTimestamp } = require("../utils/dateUtils");
const healthRepository = require("../repositories/healthRepository");

async function buildStatus() {
  const inventoryServiceReachable = await healthRepository.isInventoryServiceReachable();

  return new HealthStatus({
    status: "UP",
    service: env.serviceName,
    timestamp: getCurrentTimestamp(),
    inventoryService: inventoryServiceReachable ? "UP" : "DOWN",
  });
}

module.exports = {
  buildStatus,
};
