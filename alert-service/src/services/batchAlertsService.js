const BatchAlertItem = require("../models/BatchAlertItem");
const { env } = require("../config/env");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const productRepository = require("../repositories/productRepository");

function mapBatchRows(rows) {
  return rows.map((row) => {
    const item = new BatchAlertItem(row);
    item.daysUntilExpiration = getDaysUntilDate(item.expirationDate);
    item.urgencyStatus = resolveUrgencyStatus(item.daysUntilExpiration);
    return item;
  });
}

function resolveUrgencyStatus(daysUntilExpiration) {
  if (daysUntilExpiration < 0) {
    return "VENCIDO";
  }
  if (daysUntilExpiration <= 15) {
    return "0-15";
  }
  if (daysUntilExpiration <= 30) {
    return "16-30";
  }
  if (daysUntilExpiration <= 60) {
    return "31-60";
  }
  return "CONTROLADO";
}

function resolveLowStockLevel(availableStock, minimumStock) {
  if (!Number.isFinite(minimumStock) || minimumStock <= 0) {
    return "Alerta";
  }
  return availableStock * 2 <= minimumStock ? "Critico" : "Alerta";
}

function toSeverity(level) {
  return level === "Critico" ? "CRITICO" : "ALERTA";
}

function calculateCoveragePercent(availableStock, minimumStock) {
  if (!Number.isFinite(minimumStock) || minimumStock <= 0) {
    return 0;
  }
  return Math.round((availableStock * 100) / minimumStock);
}

function calculateSuggestedUnits(availableStock, minimumStock) {
  const safeMinimum = Number.isFinite(minimumStock) ? minimumStock : 0;
  const safeAvailable = Number.isFinite(availableStock) ? availableStock : 0;
  const missingUnits = safeMinimum - safeAvailable;
  return safeMinimum + Math.max(missingUnits, 0);
}

async function getExpiredBatches() {
  const rows = await productRepository.findExpiredBatches();
  const items = mapBatchRows(rows).map((item) => {
    item.daysExpired = Math.abs(item.daysUntilExpiration);
    item.expiredBatchStock = item.expiredBatchStock ?? item.batchStock ?? item.availableStock;
    return item;
  });
  return {
    generatedAt: getCurrentTimestamp(),
    total: items.length,
    alerts: items,
  };
}

async function getExpiringBatches(includeExpired = false, onlyWithStock = false) {
  const rows = await productRepository.findExpiringBatches(
    env.inventory.expiringSoonDays,
    includeExpired,
    onlyWithStock,
  );
  const items = mapBatchRows(rows);
  return {
    generatedAt: getCurrentTimestamp(),
    total: items.length,
    alerts: items,
  };
}

async function getLowStockBatches(filterLevel) {
  const rows = await productRepository.findLowStockBatches();
  const items = mapBatchRows(rows).map((item) => {
    const level = resolveLowStockLevel(item.availableStock, item.minimumStock);
    const coveragePercent = calculateCoveragePercent(item.availableStock, item.minimumStock);
    const suggestedUnits = calculateSuggestedUnits(item.availableStock, item.minimumStock);

    item.lowStockLevel = level;
    item.severity = toSeverity(level);
    item.coveragePercent = coveragePercent;
    item.coverageLabel = `${coveragePercent}%`;
    item.suggestion = `Reponer ${suggestedUnits} unidades`;
    return item;
  });

  const normalizedFilter = (filterLevel ?? "").trim().toLowerCase();
  const filteredItems = normalizedFilter === "critico"
    ? items.filter((item) => item.lowStockLevel === "Critico")
    : normalizedFilter === "alerta"
      ? items.filter((item) => item.lowStockLevel === "Alerta")
      : items;

  const criticalCount = items.filter((item) => item.lowStockLevel === "Critico").length;
  const alertCount = items.filter((item) => item.lowStockLevel === "Alerta").length;

  return {
    generatedAt: getCurrentTimestamp(),
    total: filteredItems.length,
    summary: {
      critical: criticalCount,
      alert: alertCount,
    },
    alerts: filteredItems,
  };
}

async function getOutOfStockBatches() {
  const rows = await productRepository.findOutOfStockBatches();
  const items = mapBatchRows(rows);
  return {
    generatedAt: getCurrentTimestamp(),
    total: items.length,
    alerts: items,
  };
}

async function getAlertsBatchesReport() {
  const [expired, expiring, lowStock, outOfStock] = await Promise.all([
    getExpiredBatches(),
    getExpiringBatches(),
    getLowStockBatches(),
    getOutOfStockBatches(),
  ]);

  return {
    generatedAt: getCurrentTimestamp(),
    total: expired.total + expiring.total + lowStock.total + outOfStock.total,
    summary: {
      expiredBatches: expired.total,
      expiringBatches: expiring.total,
      lowStockBatches: lowStock.total,
      outOfStockBatches: outOfStock.total,
    },
    reports: {
      expired: expired.alerts,
      expiring: expiring.alerts,
      lowStock: lowStock.alerts,
      outOfStock: outOfStock.alerts,
    },
  };
}

module.exports = {
  getExpiredBatches,
  getExpiringBatches,
  getLowStockBatches,
  getOutOfStockBatches,
  getAlertsBatchesReport,
};
