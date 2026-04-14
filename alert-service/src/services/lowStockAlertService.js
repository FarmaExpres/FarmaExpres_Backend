const Alert = require("../models/Alert");
const AlertCollection = require("../models/AlertCollection");
const Product = require("../models/Product");
const { ALERT_TYPES } = require("../config/constants");
const { resolveLowStockSeverity } = require("../utils/alertUtils");
const { getCurrentTimestamp } = require("../utils/dateUtils");
const productRepository = require("../repositories/productRepository");

async function getLowStockAlerts() {
  const products = await productRepository.findLowStockBatches();

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
      type: ALERT_TYPES.LOW_STOCK,
      severity: resolveLowStockSeverity(product.stock, product.minimumStock),
      message: `Producto bajo stock minimo: ${product.name}`,
      product,
    });
  });

  return new AlertCollection({
    generatedAt: getCurrentTimestamp(),
    alerts,
  });
}

module.exports = {
  getLowStockAlerts,
};
