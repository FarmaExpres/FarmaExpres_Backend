const Product = require("../models/Product");
const productRepository = require("../repositories/productRepository");
const { getCurrentTimestamp, getDaysUntilDate } = require("../utils/dateUtils");
const {
  matchesExpirationReportRange,
  resolveExpirationReportStatus,
} = require("../utils/alertUtils");

const MAX_REPORT_DAYS = 60;
const AVAILABLE_RANGES = ["all", "expired", "0-15", "16-30", "31-60"];

function normalizeRange(range) {
  if (!range || !AVAILABLE_RANGES.includes(range)) {
    return "all";
  }

  return range;
}

function buildSummary(items) {
  return {
    expired: items.filter((item) => item.diasRestantes < 0).length,
    range0To15: items.filter(
      (item) => item.diasRestantes >= 0 && item.diasRestantes <= 15,
    ).length,
    range16To30: items.filter(
      (item) => item.diasRestantes >= 16 && item.diasRestantes <= 30,
    ).length,
    range31To60: items.filter(
      (item) => item.diasRestantes >= 31 && item.diasRestantes <= 60,
    ).length,
  };
}

async function getExpiringProductsReport(range) {
  const appliedRange = normalizeRange(range);
  const productRows = await productRepository.findExpiringReportProducts(MAX_REPORT_DAYS);

  const reports = productRows
    .map((productRow) => {
      const product = new Product(productRow);
      const diasRestantes = getDaysUntilDate(product.expirationDate);

      return {
        ...product,
        diasRestantes,
        estado: resolveExpirationReportStatus(diasRestantes),
      };
    })
    .filter((item) => matchesExpirationReportRange(item.diasRestantes, appliedRange));

  return {
    generatedAt: getCurrentTimestamp(),
    total: reports.length,
    filters: {
      appliedRange,
      availableRanges: AVAILABLE_RANGES,
    },
    summary: buildSummary(reports),
    reports,
  };
}

module.exports = {
  AVAILABLE_RANGES,
  getExpiringProductsReport,
};
