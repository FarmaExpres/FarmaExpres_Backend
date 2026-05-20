const expiringProductsReportService = require("../services/expiringProductsReportService");

async function getExpiringProductsReport(request, response, next) {
  try {
    const payload = await expiringProductsReportService.getExpiringProductsReport(
      request.query.range,
      request.get("Authorization"),
    );
    return response.status(200).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getExpiringProductsReport,
};
