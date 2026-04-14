const outOfStockAlertService = require("../services/outOfStockAlertService");

async function getOutOfStockAlerts(_request, response, next) {
  try {
    const payload = await outOfStockAlertService.getOutOfStockAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getOutOfStockAlerts,
};
