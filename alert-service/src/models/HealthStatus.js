class HealthStatus {
  constructor({ status, service, timestamp, inventoryService }) {
    this.status = status;
    this.service = service;
    this.timestamp = timestamp;
    this.inventoryService = inventoryService;
  }
}

module.exports = HealthStatus;
