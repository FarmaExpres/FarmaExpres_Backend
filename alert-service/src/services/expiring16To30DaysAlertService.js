const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_TYPES } = require("../config/constants");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const { resolveExpiringSoonSeverity } = require("../utils/alertUtils");
const productRepository = require("../repositories/productRepository");

async function getExpiring16To30DaysAlerts() {
  const products = await productRepository.findProductsExpiringBetweenDays(16, 30);

  const alerts = products.map((productRow) => {
    const product = new Product(productRow);
    const daysLeft = getDaysUntilDate(product.expirationDate);

    return new Alert({
      type: ALERT_TYPES.EXPIRING_16_30_DAYS,
      severity: resolveExpiringSoonSeverity(product.expirationDate),
      message: `Producto proximo a vencer entre 16 y 30 dias (${daysLeft} dias): ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getExpiring16To30DaysAlerts,
};
