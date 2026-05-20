const { env } = require("../config/env");

async function isInventoryServiceReachable() {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), env.inventory.requestTimeoutMs);

  try {
    const response = await fetch(new URL("/status", env.inventory.serviceUrl), {
      method: "GET",
      signal: controller.signal,
    });
    return response.ok;
  } catch (_error) {
    return false;
  } finally {
    clearTimeout(timeout);
  }
}

module.exports = {
  isInventoryServiceReachable,
};
