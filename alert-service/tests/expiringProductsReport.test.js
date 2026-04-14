const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const productRepository = require("../src/repositories/productRepository");
const { app } = require("../src/app");
const { getDaysUntilDate } = require("../src/utils/dateUtils");

function formatDateFromToday(offsetDays) {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + offsetDays);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function requestJson(url) {
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

test("GET /api/alerts/expiring-report returns consolidated expiration report", async () => {
  const originalFindExpiringReportProducts = productRepository.findExpiringReportProducts;
  const expiredDate = formatDateFromToday(-50);
  const criticalDate = formatDateFromToday(7);
  const mediumDateA = formatDateFromToday(17);
  const mediumDateB = formatDateFromToday(27);
  const controlledDate = formatDateFromToday(34);

  productRepository.findExpiringReportProducts = async () => [
    {
      id: 10,
      code: "LOS-001",
      name: "Losartan 50 mg",
      stock: 75,
      minimumStock: 10,
      expirationDate: expiredDate,
      active: true,
    },
    {
      id: 11,
      code: "LOT-001",
      name: "Loratadina 10 mg",
      stock: 9,
      minimumStock: 5,
      expirationDate: criticalDate,
      active: true,
    },
    {
      id: 12,
      code: "SIM-001",
      name: "Simvastatina 20 mg",
      stock: 75,
      minimumStock: 15,
      expirationDate: mediumDateA,
      active: true,
    },
    {
      id: 13,
      code: "VIC-001",
      name: "Vitamina C 1 g",
      stock: 110,
      minimumStock: 20,
      expirationDate: mediumDateB,
      active: true,
    },
    {
      id: 14,
      code: "SAT-001",
      name: "Salbutamol Inhalador",
      stock: 35,
      minimumStock: 8,
      expirationDate: controlledDate,
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(
      `http://127.0.0.1:${address.port}/api/alerts/expiring-report`,
    );

    assert.equal(response.statusCode, 200);
    assert.ok(Date.parse(response.body.generatedAt));
    assert.equal(response.body.total, 5);
    assert.equal(response.body.filters.appliedRange, "all");
    assert.deepEqual(response.body.filters.availableRanges, [
      "all",
      "expired",
      "0-15",
      "16-30",
      "31-60",
    ]);
    assert.deepEqual(response.body.summary, {
      expired: 1,
      range0To15: 1,
      range16To30: 2,
      range31To60: 1,
    });
    assert.equal(response.body.reports[0].code, "LOS-001");
    assert.equal(response.body.reports[0].diasRestantes, getDaysUntilDate(expiredDate));
    assert.equal(response.body.reports[0].estado, "Vencido");
    assert.equal(response.body.reports[1].estado, "Critico");
    assert.equal(response.body.reports[2].estado, "Medio");
    assert.equal(response.body.reports[4].estado, "Controlado");
  } finally {
    productRepository.findExpiringReportProducts = originalFindExpiringReportProducts;

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

test("GET /api/alerts/expiring-report filters by requested range", async () => {
  const originalFindExpiringReportProducts = productRepository.findExpiringReportProducts;

  productRepository.findExpiringReportProducts = async () => [
    {
      id: 21,
      code: "EXP-001",
      name: "Expirado",
      stock: 5,
      minimumStock: 2,
      expirationDate: formatDateFromToday(-3),
      active: true,
    },
    {
      id: 22,
      code: "CRI-001",
      name: "Critico",
      stock: 8,
      minimumStock: 4,
      expirationDate: formatDateFromToday(10),
      active: true,
    },
    {
      id: 23,
      code: "MED-001",
      name: "Medio",
      stock: 10,
      minimumStock: 4,
      expirationDate: formatDateFromToday(22),
      active: true,
    },
    {
      id: 24,
      code: "CON-001",
      name: "Controlado",
      stock: 16,
      minimumStock: 4,
      expirationDate: formatDateFromToday(45),
      active: true,
    },
  ];

  const server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));
  const address = server.address();

  try {
    const response = await requestJson(
      `http://127.0.0.1:${address.port}/api/alerts/expiring-report?range=16-30`,
    );

    assert.equal(response.statusCode, 200);
    assert.equal(response.body.total, 1);
    assert.equal(response.body.filters.appliedRange, "16-30");
    assert.deepEqual(response.body.summary, {
      expired: 0,
      range0To15: 0,
      range16To30: 1,
      range31To60: 0,
    });
    assert.equal(response.body.reports.length, 1);
    assert.equal(response.body.reports[0].code, "MED-001");
    assert.equal(response.body.reports[0].estado, "Medio");
  } finally {
    productRepository.findExpiringReportProducts = originalFindExpiringReportProducts;

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
