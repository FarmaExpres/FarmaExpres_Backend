const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");

test("GET /api/alerts/expiring-half-month returns expiring alerts between 16 and 30 days", async () => {
  const originalFindProductsExpiringBetweenDays =
    productRepository.findProductsExpiringBetweenDays;

  productRepository.findProductsExpiringBetweenDays = async () => [
    {
      id: 41,
      code: "EXP-1630-001",
      name: "Cetirizina 10 mg",
      stock: 16,
      minimumStock: 6,
      expirationDate: "2026-04-20",
      active: true,
    },
    {
      id: 42,
      code: "EXP-1630-002",
      name: "Amoxicilina 500 mg",
      stock: 9,
      minimumStock: 4,
      expirationDate: "2026-04-29",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/expiring-half-month`,
        (result) => {
          let body = "";

          result.on("data", (chunk) => {
            body += chunk;
          });

          result.on("end", () => {
            resolve({
              statusCode: result.statusCode,
              body: JSON.parse(body),
            });
          });
        },
      ).on("error", reject);
    });

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 2);
    assert.equal(response.body.alerts[0].type, "EXPIRING_16_30_DAYS");
    assert.equal(response.body.alerts[0].product.code, "EXP-1630-001");
    assert.equal(response.body.alerts[0].product.expirationDate, "2026-04-20");
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate("2026-04-20"),
    );
    assert.equal(response.body.alerts[0].product.estado, "Medio");
    assert.equal(response.body.alerts[1].type, "EXPIRING_16_30_DAYS");
    assert.equal(response.body.alerts[1].product.code, "EXP-1630-002");
  } finally {
    productRepository.findProductsExpiringBetweenDays =
      originalFindProductsExpiringBetweenDays;

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
