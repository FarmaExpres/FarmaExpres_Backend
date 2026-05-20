const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { requestJson } = require("./helpers");

test("GET /api/alerts/low-stock returns alert collection", async () => {
  const originalFindLowStockBatches = inventoryClient.findLowStockBatches;

  inventoryClient.findLowStockBatches = async () => [
    {
      productId: 12,
      productCode: "MED-001",
      productName: "Acetaminofen 500mg",
      availableStock: 7,
      minimumStock: 8,
      expirationDate: "2027-06-15",
      batchId: 101,
      batchCode: "LOT-001",
      status: "ACTIVE",
    },
    {
      productId: 25,
      productCode: "MED-010",
      productName: "Ibuprofeno 400mg",
      availableStock: 2,
      minimumStock: 8,
      expirationDate: "2026-12-01",
      batchId: 102,
      batchCode: "LOT-002",
      status: "ACTIVE",
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/low-stock`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "LOW_STOCK");
    assert.equal(response.body.alerts[0].severity, "MEDIUM");
    assert.equal(
      response.body.alerts[0].message,
      "Producto bajo stock minimo: Acetaminofen 500mg",
    );
    assert.deepEqual(response.body.alerts[0].product, {
      id: "12",
      code: "MED-001",
      name: "Acetaminofen 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2027-06-15",
      active: true,
      batchId: "101",
      batchCode: "LOT-001",
      batchStatus: "ACTIVE",
    });
    assert.equal(response.body.alerts[1].severity, "HIGH");
  } finally {
    inventoryClient.findLowStockBatches = originalFindLowStockBatches;

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
