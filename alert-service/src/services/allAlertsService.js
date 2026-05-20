const expiringSoonAlertService = require("./expiringSoonAlertService");
const expiredAlertService = require("./expiredAlertService");
const lowStockAlertService = require("./lowStockAlertService");
const outOfStockAlertService = require("./outOfStockAlertService");
const { getCurrentTimestamp } = require("../utils/dateUtils");

async function getAllAlerts(authorizationHeader) {
  const [
    outOfStockResult,
    expiredResult,
    lowStockResult,
    expiringSoonResult,
  ] = await Promise.all([
    outOfStockAlertService.getOutOfStockAlerts(authorizationHeader),
    expiredAlertService.getExpiredAlerts(authorizationHeader),
    lowStockAlertService.getLowStockAlerts(authorizationHeader),
    expiringSoonAlertService.getExpiringSoonAlerts(authorizationHeader),
  ]);

  const outOfStockCount = outOfStockResult.alerts.length;
  const expiredCount = expiredResult.alerts.length;
  const lowStockCount = lowStockResult.alerts.length;
  const expiringSoonCount = expiringSoonResult.alerts.length;

  const alerts = [
    ...outOfStockResult.alerts,
    ...expiredResult.alerts,
    ...lowStockResult.alerts,
    ...expiringSoonResult.alerts,
  ];

  return {
    generatedAt: getCurrentTimestamp(),
    summary: {
      totalAlerts: alerts.length,
      outOfStock: outOfStockCount,
      expired: expiredCount,
      lowStock: lowStockCount,
      expiringSoon: expiringSoonCount,
    },
    alerts,
  };
}

module.exports = {
  getAllAlerts,
};
