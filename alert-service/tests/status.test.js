const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const { app } = require("../src/app");

test("GET /status returns service health payload", async () => {
  const server = app.listen(0);

  await new Promise((resolve) => server.once("listening", resolve));

  const address = server.address();

  try {
    const response = await new Promise((resolve, reject) => {
      http.get(
        `http://127.0.0.1:${address.port}/status`,
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
    assert.equal(response.body.status, "UP");
    assert.equal(response.body.service, "FarmaExpres_Micro_Alert");
    assert.ok(Date.parse(response.body.timestamp));
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
