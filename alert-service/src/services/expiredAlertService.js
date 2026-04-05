const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_SEVERITIES, ALERT_TYPES } = require("../config/constants");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const { resolveExpirationReportStatus } = require("../utils/alertUtils");
const productRepository = require("../repositories/productRepository");

async function getExpiredAlerts() {
  const products = await productRepository.findExpiredBatches();

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
