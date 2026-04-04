function getCurrentTimestamp() {
  return new Date().toISOString();
}

function parseDateValue(dateValue) {
  if (typeof dateValue === "string" && /^\d{4}-\d{2}-\d{2}$/.test(dateValue)) {
    const [year, month, day] = dateValue.split("-").map(Number);
    return new Date(year, month - 1, day);
  }

  return new Date(dateValue);
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

function startOfDay(dateValue) {
  const date = parseDateValue(dateValue);
  date.setHours(0, 0, 0, 0);
  return date;
}

function getDaysUntilDate(dateValue, now = new Date()) {
  const target = startOfDay(dateValue);
  const current = startOfDay(now);
  const msPerDay = 24 * 60 * 60 * 1000;
  return Math.floor((target - current) / msPerDay);
}

module.exports = {
  formatDateOnly,
  getCurrentTimestamp,
  getDaysUntilDate,
};
