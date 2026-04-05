const { Router } = require("express");
const allAlertsController = require("../controllers/allAlertsController");
const expiringProductsReportController = require("../controllers/expiringProductsReportController");
const expiring16To30DaysAlertController = require("../controllers/expiring16To30DaysAlertController");
const expiring31To60DaysAlertController = require("../controllers/expiring31To60DaysAlertController");
const expiringSoonAlertController = require("../controllers/expiringSoonAlertController");
const expiredAlertController = require("../controllers/expiredAlertController");
const lowStockAlertController = require("../controllers/lowStockAlertController");
const outOfStockAlertController = require("../controllers/outOfStockAlertController");
const batchAlertsController = require("../controllers/batchAlertsController");
const { requireReportsRole } = require("../middlewares/reportsAccessMiddleware");

const router = Router();

router.get("/api/alerts", allAlertsController.getAllAlerts);
router.get(
  "/api/alerts/expiring-report",
  expiringProductsReportController.getExpiringProductsReport,
);
router.get(
  "/api/alerts/expiring-half-month",
  expiring16To30DaysAlertController.getExpiring16To30DaysAlerts,
);
router.get(
  "/api/alerts/expiring-month",
  expiring31To60DaysAlertController.getExpiring31To60DaysAlerts,
);
router.get("/api/alerts/expiring-soon", expiringSoonAlertController.getExpiringSoonAlerts);
router.get("/api/alerts/expired", expiredAlertController.getExpiredAlerts);
router.get("/api/alerts/low-stock", lowStockAlertController.getLowStockAlerts);
router.get("/api/alerts/out-of-stock", outOfStockAlertController.getOutOfStockAlerts);
router.get("/api/alerts/expired-batches", requireReportsRole, batchAlertsController.getExpiredBatches);
router.get("/api/alerts/expiring-batches", requireReportsRole, batchAlertsController.getExpiringBatches);
router.get("/api/alerts/expiring-batches/report", requireReportsRole, batchAlertsController.getExpiringBatchesForReport);
router.get("/api/alerts/low-stock-batches", requireReportsRole, batchAlertsController.getLowStockBatches);
router.get("/api/alerts/low-stock-batches/critical", requireReportsRole, batchAlertsController.getCriticalLowStockBatches);
router.get("/api/alerts/low-stock-batches/alert", requireReportsRole, batchAlertsController.getAlertLowStockBatches);
router.get("/api/alerts/out-of-stock-batches", requireReportsRole, batchAlertsController.getOutOfStockBatches);

module.exports = router;
