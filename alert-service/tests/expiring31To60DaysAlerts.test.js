const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");

test("GET /api/alerts/expiring-month returns expiring alerts between 31 and 60 days", async () => {
  const originalFindProductsExpiringBetweenDays =
    productRepository.findProductsExpiringBetweenDays;

  productRepository.findProductsExpiringBetweenDays = async () => [
    {
      id: 51,
      code: "EXP-3160-001",
      name: "Vitamina C 1 g",
      stock: 25,
      minimumStock: 8,
      expirationDate: "2026-05-10",
      active: true,
    },
    {
      id: 52,
      code: "EXP-3160-002",
      name: "Omeprazol 20 mg",
      stock: 14,
      minimumStock: 6,
      expirationDate: "2026-05-25",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/expiring-month`,
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
    assert.equal(response.body.alerts[0].type, "EXPIRING_31_60_DAYS");
    assert.equal(response.body.alerts[0].product.code, "EXP-3160-001");
    assert.equal(response.body.alerts[0].product.expirationDate, "2026-05-10");
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate("2026-05-10"),
    );
    assert.equal(response.body.alerts[0].product.estado, "Controlado");
    assert.equal(response.body.alerts[1].type, "EXPIRING_31_60_DAYS");
    assert.equal(response.body.alerts[1].product.code, "EXP-3160-002");
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
