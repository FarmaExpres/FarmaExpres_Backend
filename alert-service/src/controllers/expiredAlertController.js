const expiredAlertService = require("../services/expiredAlertService");

async function getExpiredAlerts(request, response, next) {
  try {
    const payload = await expiredAlertService.getExpiredAlerts(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiredAlerts,
};
