const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { requestJson } = require("./helpers");

test("GET /api/alerts returns aggregated alert summary with expiringSoon", async () => {
  const originalFindOutOfStockBatches = inventoryClient.findOutOfStockBatches;
  const originalFindExpiredBatches = inventoryClient.findExpiredBatches;
  const originalFindLowStockBatches = inventoryClient.findLowStockBatches;
  const originalFindExpiringBatches = inventoryClient.findExpiringBatches;

  inventoryClient.findOutOfStockBatches = async () => [
    {
      productId: 1,
      productCode: "OUT-001",
      productName: "Producto Sin Stock",
      availableStock: 0,
      minimumStock: 5,
      expirationDate: "2026-04-10",
      batchId: 501,
      batchCode: "LOT-OUT",
      status: "OUT_OF_STOCK",
    },
  ];

  inventoryClient.findExpiredBatches = async () => [
    {
      productId: 2,
      productCode: "EXP-001",
      productName: "Producto Vencido",
      availableStock: 2,
      minimumStock: 5,
      expirationDate: "2026-03-01",
      batchId: 502,
      batchCode: "LOT-EXP",
      status: "EXPIRED",
    },
  ];

  inventoryClient.findLowStockBatches = async () => [
    {
      productId: 3,
      productCode: "LOW-001",
      productName: "Producto Bajo Stock",
      availableStock: 3,
      minimumStock: 8,
      expirationDate: "2026-12-10",
      batchId: 503,
      batchCode: "LOT-LOW",
      status: "ACTIVE",
    },
  ];

  inventoryClient.findExpiringBatches = async () => [
    {
      productId: 4,
      productCode: "SOON-001",
      productName: "Producto Proximo a Vencer",
      availableStock: 12,
      minimumStock: 4,
      expirationDate: "2026-04-10",
      batchId: 504,
      batchCode: "LOT-SOON",
      status: "ACTIVE",
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.summary.totalAlerts, 4);
    assert.equal(response.body.summary.outOfStock, 1);
    assert.equal(response.body.summary.expired, 1);
    assert.equal(response.body.summary.lowStock, 1);
    assert.equal(response.body.summary.expiringSoon, 1);
    assert.equal(response.body.alerts.length, 4);
    assert.equal(response.body.alerts[0].type, "OUT_OF_STOCK");
    assert.equal(response.body.alerts[1].type, "EXPIRED");
    assert.equal(response.body.alerts[2].type, "LOW_STOCK");
    assert.equal(response.body.alerts[3].type, "EXPIRING_SOON");
  } finally {
    inventoryClient.findOutOfStockBatches = originalFindOutOfStockBatches;
    inventoryClient.findExpiredBatches = originalFindExpiredBatches;
    inventoryClient.findLowStockBatches = originalFindLowStockBatches;
    inventoryClient.findExpiringBatches = originalFindExpiringBatches;

    await new Promise((resolve, reject) => {
      server.close((error) => {
        if (error) {
          reject(error);
          return;
        }
        resolve();
      });
    });
  }
});
