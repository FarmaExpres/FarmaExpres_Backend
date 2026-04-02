const { Router } = require("express");
const allAlertsController = require("../controllers/allAlertsController");
const expiringSoonAlertController = require("../controllers/expiringSoonAlertController");
const expiredAlertController = require("../controllers/expiredAlertController");
const lowStockAlertController = require("../controllers/lowStockAlertController");
const outOfStockAlertController = require("../controllers/outOfStockAlertController");

const router = Router();

router.get("/api/alerts", allAlertsController.getAllAlerts);
router.get("/api/alerts/expiring-soon", expiringSoonAlertController.getExpiringSoonAlerts);
router.get("/api/alerts/expired", expiredAlertController.getExpiredAlerts);
router.get("/api/alerts/low-stock", lowStockAlertController.getLowStockAlerts);
router.get("/api/alerts/out-of-stock", outOfStockAlertController.getOutOfStockAlerts);

module.exports = router;
