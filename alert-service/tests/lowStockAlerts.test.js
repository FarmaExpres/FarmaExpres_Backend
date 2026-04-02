const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");

test("GET /api/alerts/low-stock returns alert collection", async () => {
  const originalFindLowStockProducts = productRepository.findLowStockProducts;

  productRepository.findLowStockProducts = async () => [
    {
      id: 12,
      code: "MED-001",
      name: "Acetaminofen 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2027-06-15",
      active: true,
    },
    {
      id: 25,
      code: "MED-010",
      name: "Ibuprofeno 400mg",
      stock: 2,
      minimumStock: 8,
      expirationDate: "2026-12-01",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/api/alerts/low-stock`,
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
    assert.equal(response.body.alerts[0].type, "LOW_STOCK");
    assert.equal(response.body.alerts[0].severity, "MEDIUM");
    assert.equal(
      response.body.alerts[0].message,
      "Producto bajo stock minimo: Acetaminofen 500mg",
    );
    assert.deepEqual(response.body.alerts[0].product, {
      id: "12",
      code: "MED-001",
      name: "Acetaminofen 500mg",
      stock: 7,
      minimumStock: 8,
      expirationDate: "2027-06-15",
      active: true,
    });
    assert.equal(response.body.alerts[1].severity, "HIGH");
  } finally {
    productRepository.findLowStockProducts = originalFindLowStockProducts;

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
