function getCurrentTimestamp() {
  return new Date().toISOString();
}

function startOfDay(dateValue) {
  const date = new Date(dateValue);
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
  getCurrentTimestamp,
  getDaysUntilDate,
};
