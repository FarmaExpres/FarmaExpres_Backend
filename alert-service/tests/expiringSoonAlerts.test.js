const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");

test("GET /api/alerts/expiring-soon returns expiring soon alert collection", async () => {
  const originalFindExpiringSoonProducts = productRepository.findExpiringSoonProducts;

  productRepository.findExpiringSoonProducts = async () => [
    {
      id: 33,
      code: "EXP-001",
      name: "Loratadina 10 mg",
      stock: 12,
      minimumStock: 5,
      expirationDate: "2026-04-10",
      active: true,
    },
    {
      id: 34,
      code: "EXP-002",
      name: "Diclofenaco 50 mg",
      stock: 8,
      minimumStock: 4,
      expirationDate: "2026-04-14",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/expiring-soon`,
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
    assert.equal(response.body.alerts[0].type, "EXPIRING_SOON");
    assert.equal(response.body.alerts[0].product.code, "EXP-001");
    assert.equal(response.body.alerts[0].product.expirationDate, "2026-04-10");
    assert.equal(
      response.body.alerts[0].product.diasRestantes,
      getDaysUntilDate("2026-04-10"),
    );
    assert.equal(response.body.alerts[0].product.estado, "Critico");
    assert.equal(response.body.alerts[1].type, "EXPIRING_SOON");
    assert.equal(response.body.alerts[1].product.code, "EXP-002");
  } finally {
    productRepository.findExpiringSoonProducts = originalFindExpiringSoonProducts;

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
