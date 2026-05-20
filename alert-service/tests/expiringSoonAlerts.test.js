const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");
const { requestJson } = require("./helpers");

test("GET /api/alerts/expiring-soon returns expiring soon alert collection", async () => {
  const originalFindExpiringBatches = inventoryClient.findExpiringBatches;

  inventoryClient.findExpiringBatches = async () => [
    {
      productId: 33,
      productCode: "EXP-001",
      productName: "Loratadina 10 mg",
      availableStock: 12,
      minimumStock: 5,
      expirationDate: "2026-04-10",
      batchId: 401,
      batchCode: "LOT-SOON",
      status: "ACTIVE",
    },
    {
      productId: 34,
      productCode: "EXP-002",
      productName: "Diclofenaco 50 mg",
      availableStock: 8,
      minimumStock: 4,
      expirationDate: "2026-04-14",
      batchId: 402,
      batchCode: "LOT-SOON-2",
      status: "ACTIVE",
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/expiring-soon`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "EXPIRING_SOON");
    assert.equal(response.body.alerts[0].product.code, "EXP-001");
    assert.equal(response.body.alerts[0].product.expirationDate, "2026-04-10");
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate("2026-04-10"),
    );
    assert.equal(response.body.alerts[0].product.estado, "Vencido");
    assert.equal(response.body.alerts[1].type, "EXPIRING_SOON");
    assert.equal(response.body.alerts[1].product.code, "EXP-002");
  } finally {
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
