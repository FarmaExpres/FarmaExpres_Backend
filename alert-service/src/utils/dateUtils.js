const { env } = require("../config/env");

function getCurrentTimestamp() {
  return new Date().toISOString();
}

function isDateOnlyString(dateValue) {
  return typeof dateValue === "string" && /^\d{4}-\d{2}-\d{2}$/.test(dateValue);
}

function parseDateValue(dateValue) {
  if (isDateOnlyString(dateValue)) {
    const [year, month, day] = dateValue.split("-").map(Number);
    return new Date(year, month - 1, day);
  }

  return new Date(dateValue);
}

function getDatePartsInTimeZone(dateValue, timeZone = env.appTimeZone) {
  if (isDateOnlyString(dateValue)) {
    const [year, month, day] = dateValue.split("-").map(Number);
    return { year, month, day };
  }

  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

  const parts = formatter.formatToParts(parseDateValue(dateValue));
  const year = Number(parts.find((part) => part.type === "year").value);
  const month = Number(parts.find((part) => part.type === "month").value);
  const day = Number(parts.find((part) => part.type === "day").value);

  return { year, month, day };
}

function formatDateOnly(dateValue) {
  if (!dateValue) {
    return dateValue;
  }

  if (typeof dateValue === "string") {
    return dateValue.slice(0, 10);
  }

  const date = parseDateValue(dateValue);
  return date.toISOString().slice(0, 10);
}

function startOfDay(dateValue, timeZone = env.appTimeZone) {
  const { year, month, day } = getDatePartsInTimeZone(dateValue, timeZone);
  return Date.UTC(year, month - 1, day);
}

function getDaysUntilDate(dateValue, now = new Date(), timeZone = env.appTimeZone) {
  const target = startOfDay(dateValue, timeZone);
  const current = startOfDay(now, timeZone);
  const msPerDay = 24 * 60 * 60 * 1000;
  return Math.floor((target - current) / msPerDay);
}

module.exports = {
  formatDateOnly,
  getCurrentTimestamp,
  getDaysUntilDate,
};
