const { formatDateOnly } = require("../utils/dateUtils");

class Product {
  constructor({
    id,
    code,
    name,
    stock,
    minimumStock,
    expirationDate,
    active,
    batchId,
    batchCode,
    batchStatus,
  }) {
    this.id = String(id);
    this.code = code;
    this.name = name;
    this.stock = Number(stock);
    this.minimumStock = Number(minimumStock);
    this.expirationDate = formatDateOnly(expirationDate);
    this.active = Boolean(active);
    this.batchId = batchId != null ? String(batchId) : null;
    this.batchCode = batchCode ?? null;
    this.batchStatus = batchStatus ?? null;
  }
}

module.exports = Product;
