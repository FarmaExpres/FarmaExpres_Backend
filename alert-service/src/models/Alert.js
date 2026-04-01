class Alert {
  constructor({ type, severity, message, product }) {
    this.type = type;
    this.severity = severity;
    this.message = message;
    this.product = product;
  }
}

module.exports = Alert;
