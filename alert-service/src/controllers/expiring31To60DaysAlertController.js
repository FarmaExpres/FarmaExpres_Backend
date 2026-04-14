const expiring31To60DaysAlertService = require("../services/expiring31To60DaysAlertService");

async function getExpiring31To60DaysAlerts(_request, response, next) {
  try {
    const payload = await expiring31To60DaysAlertService.getExpiring31To60DaysAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiring31To60DaysAlerts,
};
