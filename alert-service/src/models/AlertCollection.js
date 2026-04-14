class AlertCollection {
  constructor({ generatedAt, alerts }) {
    this.generatedAt = generatedAt;
    this.total = alerts.length;
    this.alerts = alerts;
  }
}

module.exports = AlertCollection;
