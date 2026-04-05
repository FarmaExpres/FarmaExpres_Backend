const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_SEVERITIES, ALERT_TYPES } = require("../config/constants");
const { getCurrentTimestamp } = require("../utils/dateUtils");
const productRepository = require("../repositories/productRepository");

async function getOutOfStockAlerts() {
  const products = await productRepository.findOutOfStockBatches();

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

    return new Alert({
      type: ALERT_TYPES.OUT_OF_STOCK,
      severity: ALERT_SEVERITIES.HIGH,
      message: `Producto sin stock: ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getOutOfStockAlerts,
};
