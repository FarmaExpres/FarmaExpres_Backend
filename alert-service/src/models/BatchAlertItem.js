const { formatDateOnly } = require("../utils/dateUtils");

class BatchAlertItem {
  constructor({
    productId,
    productCode,
    productName,
    batchId,
    batchCode,
    expirationDate,
    availableStock,
    batchStock,
    expiredBatchStock,
    operationalStock,
    minimumStock,
    status,
    daysUntilExpiration,
    daysExpired,
    urgencyStatus,
    lowStockLevel,
    severity,
    coveragePercent,
    coverageLabel,
    suggestion,
  }) {
    this.productId = String(productId);
    this.productCode = productCode;
    this.productName = productName;
    this.batchId = String(batchId);
    this.batchCode = batchCode;
    this.expirationDate = formatDateOnly(expirationDate);
    this.availableStock = Number(availableStock ?? 0);
    this.batchStock = Number(batchStock ?? availableStock ?? 0);
    this.expiredBatchStock = expiredBatchStock == null ? null : Number(expiredBatchStock);
    this.operationalStock = Number(operationalStock ?? 0);
    this.minimumStock = Number(minimumStock);
    this.status = status;
    this.daysUntilExpiration = daysUntilExpiration ?? null;
    this.daysExpired = daysExpired ?? null;
    this.urgencyStatus = urgencyStatus ?? null;
    this.lowStockLevel = lowStockLevel ?? null;
    this.severity = severity ?? null;
    this.coveragePercent = coveragePercent ?? null;
    this.coverageLabel = coverageLabel ?? null;
    this.suggestion = suggestion ?? null;
  }
}

module.exports = BatchAlertItem;
