const expiring16To30DaysAlertService = require("../services/expiring16To30DaysAlertService");

async function getExpiring16To30DaysAlerts(_request, response, next) {
  try {
    const payload = await expiring16To30DaysAlertService.getExpiring16To30DaysAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiring16To30DaysAlerts,
};
