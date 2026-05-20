const crypto = require("crypto");
const http = require("node:http");

const TEST_JWT_SECRET = "test_secret_256_bits_minimo_para_alert_service";
process.env.JWT_SECRET = process.env.JWT_SECRET || TEST_JWT_SECRET;

function createToken(role = "ADMIN") {
  const header = Buffer.from(JSON.stringify({ alg: "HS256", typ: "JWT" })).toString("base64url");
  const payload = Buffer.from(JSON.stringify({
    sub: "test-user",
    role,
    exp: Math.floor(Date.now() / 1000) + 3600,
  })).toString("base64url");
  const signature = crypto
    .createHmac("sha256", process.env.JWT_SECRET)
    .update(`${header}.${payload}`)
    .digest("base64url");

  return `${header}.${payload}.${signature}`;
}

function authHeaders(role = "ADMIN") {
  return {
    Authorization: `Bearer ${createToken(role)}`,
  };
}

function requestJson(url, role = "ADMIN") {
  return new Promise((resolve, reject) => {
    http.get(url, { headers: authHeaders(role) }, (result) => {
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

module.exports = {
  authHeaders,
  requestJson,
};
