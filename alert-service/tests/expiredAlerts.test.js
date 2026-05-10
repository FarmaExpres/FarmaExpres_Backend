const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");
const { requestJson } = require("./helpers");

test("GET /api/alerts/expired returns expired alert collection", async () => {
  const originalFindExpiredBatches = inventoryClient.findExpiredBatches;

  inventoryClient.findExpiredBatches = async () => [
    {
      productId: 13,
      productCode: "M-001",
      productName: "Acetaminofen 500mg",
      availableStock: 7,
      minimumStock: 8,
      expirationDate: "2026-01-15",
      batchId: 301,
      batchCode: "LOT-EXP",
      status: "EXPIRED",
    },
    {
      productId: 14,
      productCode: "M-010",
      productName: "Loratadina 10mg",
      availableStock: 4,
      minimumStock: 5,
      expirationDate: "2026-03-01",
      batchId: 302,
      batchCode: "LOT-EXP-2",
      status: "EXPIRED",
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/expired`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "EXPIRED");
    assert.equal(response.body.alerts[0].severity, "HIGH");
    assert.equal(response.body.alerts[0].message, "Producto vencido: Acetaminofen 500mg");
    assert.deepEqual(response.body.alerts[0].product, {
      id: "13",
      code: "M-001",
      name: "Acetaminofen 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2026-01-15",
      active: true,
      batchId: "301",
      batchCode: "LOT-EXP",
      batchStatus: "EXPIRED",
      diasRestantes: getDaysUntilDate("2026-01-15"),
      estado: "Vencido",
    });
  } finally {
    inventoryClient.findExpiredBatches = originalFindExpiredBatches;

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
