const allAlertsService = require("../services/allAlertsService");

async function getAllAlerts(request, response, next) {
  try {
    const payload = await allAlertsService.getAllAlerts(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getAllAlerts,
};
