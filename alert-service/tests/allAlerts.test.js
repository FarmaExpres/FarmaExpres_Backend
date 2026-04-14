const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");

test("GET /api/alerts returns aggregated alert summary with expiringSoon", async () => {
  const originalFindOutOfStockProducts = productRepository.findOutOfStockProducts;
  const originalFindExpiredProducts = productRepository.findExpiredProducts;
  const originalFindLowStockProducts = productRepository.findLowStockProducts;
  const originalFindExpiringSoonProducts = productRepository.findExpiringSoonProducts;

  productRepository.findOutOfStockProducts = async () => [
    {
      id: 1,
      code: "OUT-001",
      name: "Producto Sin Stock",
      stock: 0,
      minimumStock: 5,
      expirationDate: "2026-04-10",
      active: true,
    },
  ];

  productRepository.findExpiredProducts = async () => [
    {
      id: 2,
      code: "EXP-001",
      name: "Producto Vencido",
      stock: 2,
      minimumStock: 5,
      expirationDate: "2026-03-01",
      active: true,
    },
  ];

  productRepository.findLowStockProducts = async () => [
    {
      id: 3,
      code: "LOW-001",
      name: "Producto Bajo Stock",
      stock: 3,
      minimumStock: 8,
      expirationDate: "2026-12-10",
      active: true,
    },
  ];

  productRepository.findExpiringSoonProducts = async () => [
    {
      id: 4,
      code: "SOON-001",
      name: "Producto Proximo a Vencer",
      stock: 12,
      minimumStock: 4,
      expirationDate: "2026-04-10",
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(`http://127.0.0.1:${address.port}/api/alerts`, (result) => {
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
      }).on("error", reject);
    });

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.summary.totalAlerts, 4);
    assert.equal(response.body.summary.outOfStock, 1);
    assert.equal(response.body.summary.expired, 1);
    assert.equal(response.body.summary.lowStock, 1);
    assert.equal(response.body.summary.expiringSoon, 1);
    assert.equal(response.body.alerts.length, 4);
    assert.equal(response.body.alerts[0].type, "OUT_OF_STOCK");
    assert.equal(response.body.alerts[1].type, "EXPIRED");
    assert.equal(response.body.alerts[2].type, "LOW_STOCK");
    assert.equal(response.body.alerts[3].type, "EXPIRING_SOON");
  } finally {
    productRepository.findOutOfStockProducts = originalFindOutOfStockProducts;
    productRepository.findExpiredProducts = originalFindExpiredProducts;
    productRepository.findLowStockProducts = originalFindLowStockProducts;
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
