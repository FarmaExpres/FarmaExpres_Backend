const batchAlertsService = require("../services/batchAlertsService");

async function getAlertsBatchesReport(_request, response, next) {
  try {
    const payload = await batchAlertsService.getAlertsBatchesReport();
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getAlertsBatchesReport,
};

