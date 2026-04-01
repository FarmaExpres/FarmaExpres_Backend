const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_SEVERITIES, ALERT_TYPES } = require("../config/constants");
const { getCurrentTimestamp } = require("../utils/dateUtils");
const productRepository = require("../repositories/productRepository");

async function getExpiredAlerts() {
  const products = await productRepository.findExpiredProducts();

  const alerts = products.map((productRow) => {
    const product = new Product(productRow);

    return new Alert({
      type: ALERT_TYPES.EXPIRED,
      severity: ALERT_SEVERITIES.HIGH,
      message: `Producto vencido: ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getExpiredAlerts,
};
