const expiringSoonAlertService = require("../services/expiringSoonAlertService");

async function getExpiringSoonAlerts(request, response, next) {
  try {
    const payload = await expiringSoonAlertService.getExpiringSoonAlerts(request.get("Authorization"));
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiringSoonAlerts,
};
