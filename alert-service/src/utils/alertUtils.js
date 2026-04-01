const { ALERT_SEVERITIES } = require("../config/constants");
const { getDaysUntilDate } = require("./dateUtils");

function resolveLowStockSeverity(stock, minimumStock) {
  const deficit = Number(minimumStock) - Number(stock);

  if (deficit >= 5) {
    return ALERT_SEVERITIES.HIGH;
  }

  if (deficit >= 1) {
    return ALERT_SEVERITIES.MEDIUM;
  }

  return ALERT_SEVERITIES.LOW;
}

function resolveExpiringSoonSeverity(expirationDate) {
  const daysUntilExpiration = getDaysUntilDate(expirationDate);

  if (daysUntilExpiration <= 5) {
    return ALERT_SEVERITIES.HIGH;
  }

  if (daysUntilExpiration <= 10) {
    return ALERT_SEVERITIES.MEDIUM;
  }

  return ALERT_SEVERITIES.LOW;
}

module.exports = {
  resolveExpiringSoonSeverity,
  resolveLowStockSeverity,
};
