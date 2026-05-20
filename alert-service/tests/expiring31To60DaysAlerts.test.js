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

test("GET /api/alerts/expiring-month returns expiring alerts between 31 and 60 days", async () => {
  const originalFindProductsExpiringBetweenDays = inventoryClient.findProductsExpiringBetweenDays;
  const firstExpirationDate = formatDateFromToday(35);
  const secondExpirationDate = formatDateFromToday(58);

  inventoryClient.findProductsExpiringBetweenDays = async () => [
    {
      id: 51,
      code: "EXP-3160-001",
      name: "Omeprazol 20 mg",
      stock: 40,
      minimumStock: 10,
      expirationDate: firstExpirationDate,
      active: true,
    },
    {
      id: 52,
      code: "EXP-3160-002",
      name: "Metformina 850 mg",
      stock: 30,
      minimumStock: 8,
      expirationDate: secondExpirationDate,
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(`http://127.0.0.1:${address.port}/api/alerts/expiring-month`);

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "EXPIRING_31_60_DAYS");
    assert.equal(response.body.alerts[0].product.code, "EXP-3160-001");
    assert.equal(response.body.alerts[0].product.expirationDate, firstExpirationDate);
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate(firstExpirationDate),
    );
    assert.equal(response.body.alerts[0].product.estado, "Controlado");
    assert.equal(response.body.alerts[1].type, "EXPIRING_31_60_DAYS");
    assert.equal(response.body.alerts[1].product.code, "EXP-3160-002");
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
