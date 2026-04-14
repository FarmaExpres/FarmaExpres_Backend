const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_TYPES } = require("../config/constants");
const { env } = require("../config/env");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const {
  resolveExpirationReportStatus,
  resolveExpiringSoonSeverity,
} = require("../utils/alertUtils");
const productRepository = require("../repositories/productRepository");

async function getExpiringSoonAlerts() {
  const products = await productRepository.findExpiringBatches(
    env.inventory.expiringSoonDays,
    false,
  );

  const alerts = products.map((productRow) => {
    const product = new Product({
      id: productRow.productId,
      code: productRow.productCode,
      name: productRow.productName,
      stock: productRow.availableStock,
      minimumStock: productRow.minimumStock,
      expirationDate: productRow.expirationDate,
      active: true,
      batchId: productRow.batchId,
      batchCode: productRow.batchCode,
      batchStatus: productRow.status,
    });
    const diasRestantes = getDaysUntilDate(product.expirationDate);
    product.diasRestantes = diasRestantes;
    product.estado = resolveExpirationReportStatus(diasRestantes);

    return new Alert({
      type: ALERT_TYPES.EXPIRING_SOON,
      severity: resolveExpiringSoonSeverity(product.expirationDate),
      message: `Producto proximo a vencer (${diasRestantes} dias): ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getExpiringSoonAlerts,
};
