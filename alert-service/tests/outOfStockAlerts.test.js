const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { requestJson } = require("./helpers");

test("GET /api/alerts/out-of-stock returns out of stock alert collection", async () => {
  const originalFindOutOfStockBatches = inventoryClient.findOutOfStockBatches;

  inventoryClient.findOutOfStockBatches = async () => [
    {
      productId: 16,
      productCode: "MASD-001",
      productName: "Acetaminofen 500mg",
      availableStock: 0,
      minimumStock: 8,
      expirationDate: "2026-03-31",
      batchId: 201,
      batchCode: "LOT-OUT",
      status: "OUT_OF_STOCK",
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/out-of-stock`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 1);
    assert.equal(response.body.alerts[0].type, "OUT_OF_STOCK");
    assert.equal(response.body.alerts[0].severity, "HIGH");
    assert.equal(response.body.alerts[0].message, "Producto sin stock: Acetaminofen 500mg");
    assert.deepEqual(response.body.alerts[0].product, {
      id: "16",
      code: "MASD-001",
      name: "Acetaminofen 500mg",
      stock: 0,
      minimumStock: 8,
      expirationDate: "2026-03-31",
      active: true,
      batchId: "201",
      batchCode: "LOT-OUT",
      batchStatus: "OUT_OF_STOCK",
    });
  } finally {
    inventoryClient.findOutOfStockBatches = originalFindOutOfStockBatches;

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
