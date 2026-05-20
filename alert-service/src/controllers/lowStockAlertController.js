const lowStockAlertService = require("../services/lowStockAlertService");

async function getLowStockAlerts(request, response, next) {
  try {
    const payload = await lowStockAlertService.getLowStockAlerts(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getLowStockAlerts,
};
