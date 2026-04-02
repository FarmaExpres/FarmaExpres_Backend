const expiredAlertService = require("../services/expiredAlertService");

async function getExpiredAlerts(_request, response, next) {
  try {
    const payload = await expiredAlertService.getExpiredAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiredAlerts,
};
