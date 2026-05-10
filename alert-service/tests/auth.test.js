const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const inventoryClient = require("../src/clients/inventoryClient");
const { app } = require("../src/app");
const { requestJson } = require("./helpers");

function requestWithoutToken(url) {
  return new Promise((resolve, reject) => {
    http.get(url, (result) => {
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
}

test("GET /api/alerts/low-stock requires authentication", async () => {
  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestWithoutToken(
      `http://127.0.0.1:${address.port}/api/alerts/low-stock`,
    );

    assert.equal(response.statusCode, 401);
    assert.equal(response.body.error, "UNAUTHORIZED");
  } finally {
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

test("GET /api/alerts/expiring-report rejects roles outside reports policy", async () => {
  const originalFindExpiringReportProducts = inventoryClient.findExpiringReportProducts;
  inventoryClient.findExpiringReportProducts = async () => [];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(
      `http://127.0.0.1:${address.port}/api/alerts/expiring-report`,
      "CLIENTE",
    );

    assert.equal(response.statusCode, 403);
    assert.equal(response.body.error, "FORBIDDEN");
  } finally {
    inventoryClient.findExpiringReportProducts = originalFindExpiringReportProducts;

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
