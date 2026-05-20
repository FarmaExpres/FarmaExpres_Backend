const batchAlertsService = require("../services/batchAlertsService");

async function getExpiredBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getExpiredBatches(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

function parseBoolean(value) {
  if (typeof value !== "string") {
    return false;
  }
  return ["1", "true", "yes", "si"].includes(value.trim().toLowerCase());
}

async function getExpiringBatches(request, response, next) {
  try {
    const includeExpired = parseBoolean(request.query.includeExpired);
    const payload = await batchAlertsService.getExpiringBatches(
      includeExpired,
      false,
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getExpiringBatchesForReport(request, response, next) {
  try {
    const payload = await batchAlertsService.getExpiringBatches(
      true,
      true,
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getLowStockBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches(
      request.query.level,
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getCriticalLowStockBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches(
      "critico",
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getAlertLowStockBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches(
      "alerta",
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getOutOfStockBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getOutOfStockBatches(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiredBatches,
  getExpiringBatches,
  getExpiringBatchesForReport,
  getLowStockBatches,
  getCriticalLowStockBatches,
  getAlertLowStockBatches,
  getOutOfStockBatches,
};
