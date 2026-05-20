const outOfStockAlertService = require("../services/outOfStockAlertService");

async function getOutOfStockAlerts(request, response, next) {
  try {
    const payload = await outOfStockAlertService.getOutOfStockAlerts(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getOutOfStockAlerts,
};
