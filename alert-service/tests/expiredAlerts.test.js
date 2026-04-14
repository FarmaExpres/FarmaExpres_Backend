const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");

test("GET /api/alerts/expired returns expired alert collection", async () => {
  const originalFindExpiredProducts = productRepository.findExpiredProducts;

  productRepository.findExpiredProducts = async () => [
    {
      id: 13,
      code: "M-001",
      name: "Acetaminofén 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2026-01-15",
      active: true,
    },
    {
      id: 14,
      code: "M-010",
      name: "Loratadina 10mg",
      stock: 4,
      minimumStock: 5,
      expirationDate: "2026-03-01",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/expired`,
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
    assert.equal(response.body.alerts[0].type, "EXPIRED");
    assert.equal(response.body.alerts[0].severity, "HIGH");
    assert.equal(
      response.body.alerts[0].message,
      "Producto vencido: Acetaminofén 500mg",
    );
    assert.deepEqual(response.body.alerts[0].product, {
      id: "13",
      code: "M-001",
      name: "Acetaminofén 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2026-01-15",
      active: true,
      diasRestantes: getDaysUntilDate("2026-01-15"),
      estado: "Vencido",
    });
  } finally {
    productRepository.findExpiredProducts = originalFindExpiredProducts;

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
