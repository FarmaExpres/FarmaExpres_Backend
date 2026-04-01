const expiringSoonAlertService = require("../services/expiringSoonAlertService");

async function getExpiringSoonAlerts(_request, response, next) {
  try {
    const payload = await expiringSoonAlertService.getExpiringSoonAlerts();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiringSoonAlerts,
};
