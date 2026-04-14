const lowStockAlertService = require("../services/lowStockAlertService");

async function getLowStockAlerts(_request, response, next) {
  try {
    const payload = await lowStockAlertService.getLowStockAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getLowStockAlerts,
};
