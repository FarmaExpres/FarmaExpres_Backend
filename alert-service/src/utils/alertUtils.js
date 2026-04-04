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

function resolveExpirationReportStatus(daysUntilExpiration) {
  if (daysUntilExpiration < 0) {
    return "Vencido";
  }

  if (daysUntilExpiration <= 15) {
    return "Critico";
  }

  if (daysUntilExpiration <= 30) {
    return "Medio";
  }

  return "Controlado";
}

function matchesExpirationReportRange(daysUntilExpiration, range = "all") {
  switch (range) {
    case "expired":
      return daysUntilExpiration < 0;
    case "0-15":
      return daysUntilExpiration >= 0 && daysUntilExpiration <= 15;
    case "16-30":
      return daysUntilExpiration >= 16 && daysUntilExpiration <= 30;
    case "31-60":
      return daysUntilExpiration >= 31 && daysUntilExpiration <= 60;
    case "all":
    default:
      return daysUntilExpiration <= 60;
  }
}

module.exports = {
  matchesExpirationReportRange,
  resolveExpirationReportStatus,
  resolveExpiringSoonSeverity,
  resolveLowStockSeverity,
};
