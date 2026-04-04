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
  }) {
    this.id = String(id);
    this.code = code;
    this.name = name;
    this.stock = Number(stock);
    this.minimumStock = Number(minimumStock);
    this.expirationDate = formatDateOnly(expirationDate);
    this.active = Boolean(active);
  }
}

module.exports = Product;
