const allAlertsService = require("../services/allAlertsService");

async function getAllAlerts(_request, response, next) {
  try {
    const payload = await allAlertsService.getAllAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getAllAlerts,
};
