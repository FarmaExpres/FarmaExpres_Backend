const { env } = require("../config/env");

class InventoryServiceError extends Error {
  constructor(message, statusCode = 502) {
    super(message);
    this.name = "InventoryServiceError";
    this.statusCode = statusCode;
  }
}

function buildUrl(path, query = {}) {
  const url = new URL(path, env.inventory.serviceUrl);

  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  return url;
}

async function getJson(path, query = {}, authorizationHeader) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), env.inventory.requestTimeoutMs);

  try {
    const headers = {
      Accept: "application/json",
    };

    if (authorizationHeader) {
      headers.Authorization = authorizationHeader;
    }

    const response = await fetch(buildUrl(path, query), {
      method: "GET",
      headers,
      signal: controller.signal,
    });

    if (!response.ok) {
      throw new InventoryServiceError(
        `inventory-service respondio con estado ${response.status}`,
        response.status >= 500 ? 502 : response.status,
      );
    }

    return response.json();
  } catch (error) {
    if (error instanceof InventoryServiceError) {
      throw error;
    }

    const message = error.name === "AbortError"
      ? "Tiempo de espera agotado consultando inventory-service"
      : "No fue posible consultar inventory-service";
    throw new InventoryServiceError(message);
  } finally {
    clearTimeout(timeout);
  }
}

function findExpiredBatches(authorizationHeader) {
  return getJson("/api/inventory/alerts/expired", {}, authorizationHeader);
}

function findExpiringBatches(daysWindow, includeExpired = false, onlyWithStock = false, authorizationHeader) {
  return getJson(
    "/api/inventory/alerts/expiring-soon",
    { days: daysWindow, includeExpired, onlyWithStock },
    authorizationHeader,
  );
}

function findLowStockBatches(authorizationHeader) {
  return getJson("/api/inventory/alerts/low-stock", {}, authorizationHeader);
}

function findOutOfStockBatches(authorizationHeader) {
  return getJson("/api/inventory/alerts/out-of-stock", {}, authorizationHeader);
}

function findExpiringReportProducts(maxDaysWindow, authorizationHeader) {
  return getJson(
    "/api/inventory/reports/expiring-products",
    { maxDays: maxDaysWindow },
    authorizationHeader,
  );
}

function findProductsExpiringBetweenDays(minDays, maxDays, authorizationHeader) {
  return getJson(
    "/api/inventory/alerts/expiring-range",
    { minDays, maxDays },
    authorizationHeader,
  );
}

module.exports = {
  InventoryServiceError,
  findExpiredBatches,
  findExpiringBatches,
  findLowStockBatches,
  findOutOfStockBatches,
  findExpiringReportProducts,
  findProductsExpiringBetweenDays,
};
