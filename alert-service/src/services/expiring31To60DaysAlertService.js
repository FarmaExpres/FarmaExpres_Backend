const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_TYPES } = require("../config/constants");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const {
  resolveExpirationReportStatus,
  resolveExpiringSoonSeverity,
} = require("../utils/alertUtils");
const productRepository = require("../repositories/productRepository");

async function getExpiring31To60DaysAlerts() {
  const products = await productRepository.findProductsExpiringBetweenDays(31, 60);

  const alerts = products.map((productRow) => {
    const product = new Product(productRow);
    const diasRestantes = getDaysUntilDate(product.expirationDate);
    product.diasRestantes = diasRestantes;
    product.estado = resolveExpirationReportStatus(diasRestantes);

    return new Alert({
      type: ALERT_TYPES.EXPIRING_31_60_DAYS,
      severity: resolveExpiringSoonSeverity(product.expirationDate),
      message: `Producto proximo a vencer entre 31 y 60 dias (${diasRestantes} dias): ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getExpiring31To60DaysAlerts,
};
