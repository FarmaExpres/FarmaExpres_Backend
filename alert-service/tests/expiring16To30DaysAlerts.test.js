const test = require("node:test");
const assert = require("node:assert/strict");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");
const { requestJson } = require("./helpers");

function formatDateFromToday(offsetDays) {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + offsetDays);
  return date.toISOString().slice(0, 10);
}

test("GET /api/alerts/expiring-half-month returns expiring alerts between 16 and 30 days", async () => {
  const originalFindProductsExpiringBetweenDays = inventoryClient.findProductsExpiringBetweenDays;
  const firstExpirationDate = formatDateFromToday(20);
  const secondExpirationDate = formatDateFromToday(29);

  inventoryClient.findProductsExpiringBetweenDays = async () => [
    {
      id: 41,
      code: "EXP-1630-001",
      name: "Cetirizina 10 mg",
      stock: 16,
      minimumStock: 6,
      expirationDate: firstExpirationDate,
      active: true,
    },
    {
      id: 42,
      code: "EXP-1630-002",
      name: "Amoxicilina 500 mg",
      stock: 9,
      minimumStock: 4,
      expirationDate: secondExpirationDate,
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/expiring-half-month`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "EXPIRING_16_30_DAYS");
    assert.equal(response.body.alerts[0].product.code, "EXP-1630-001");
    assert.equal(response.body.alerts[0].product.expirationDate, firstExpirationDate);
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate(firstExpirationDate),
    );
    assert.equal(response.body.alerts[0].product.estado, "Medio");
    assert.equal(response.body.alerts[1].type, "EXPIRING_16_30_DAYS");
    assert.equal(response.body.alerts[1].product.code, "EXP-1630-002");
  } finally {
    inventoryClient.findProductsExpiringBetweenDays = originalFindProductsExpiringBetweenDays;

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
