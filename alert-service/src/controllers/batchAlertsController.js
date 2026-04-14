const batchAlertsService = require("../services/batchAlertsService");

async function getExpiredBatches(_request, response, next) {
  try {
    const payload = await batchAlertsService.getExpiredBatches();
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
    const payload = await batchAlertsService.getExpiringBatches(includeExpired);
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getExpiringBatchesForReport(_request, response, next) {
  try {
    const payload = await batchAlertsService.getExpiringBatches(true, true);
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getLowStockBatches(request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches(request.query.level);
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getCriticalLowStockBatches(_request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches("critico");
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getAlertLowStockBatches(_request, response, next) {
  try {
    const payload = await batchAlertsService.getLowStockBatches("alerta");
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

async function getOutOfStockBatches(_request, response, next) {
  try {
    const payload = await batchAlertsService.getOutOfStockBatches();
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
