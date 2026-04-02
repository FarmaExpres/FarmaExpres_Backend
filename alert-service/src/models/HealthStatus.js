class HealthStatus {
  constructor({ status, service, timestamp, database }) {
    this.status = status;
    this.service = service;
    this.timestamp = timestamp;
    this.database = database;
  }
}

module.exports = HealthStatus;
