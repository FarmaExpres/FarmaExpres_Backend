const test = require("node:test");
const assert = require("node:assert/strict");

const { getDaysUntilDate } = require("../src/utils/dateUtils");

test("getDaysUntilDate excludes the current day and counts through expiration day", () => {
  const now = new Date("2026-04-03T10:00:00");

  assert.equal(getDaysUntilDate("2026-04-10", now), 7);
  assert.equal(getDaysUntilDate("2026-04-20", now), 17);
});

test("getDaysUntilDate respects Colombia time even when server time is already UTC next day", () => {
  const utcNow = new Date("2026-04-04T01:23:09.234Z");

  assert.equal(getDaysUntilDate("2026-04-20", utcNow), 17);
});
