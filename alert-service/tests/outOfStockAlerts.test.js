const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");

test("GET /api/alerts/out-of-stock returns out of stock alert collection", async () => {
  const originalFindOutOfStockProducts = productRepository.findOutOfStockProducts;

  productRepository.findOutOfStockProducts = async () => [
    {
      id: 16,
      code: "Masd-001",
      name: "Acetaminofén 500mg",
      stock: 0,
      minimumStock: 8,
      expirationDate: "2026-03-31",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/out-of-stock`,
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
    assert.equal(response.body.total, 1);
    assert.equal(response.body.alerts[0].type, "OUT_OF_STOCK");
    assert.equal(response.body.alerts[0].severity, "HIGH");
    assert.equal(
      response.body.alerts[0].message,
      "Producto sin stock: Acetaminofén 500mg",
    );
    assert.deepEqual(response.body.alerts[0].product, {
      id: "16",
      code: "Masd-001",
      name: "Acetaminofén 500mg",
      stock: 0,
      minimumStock: 8,
      expirationDate: "2026-03-31",
      active: true,
    });
  } finally {
    productRepository.findOutOfStockProducts = originalFindOutOfStockProducts;

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
