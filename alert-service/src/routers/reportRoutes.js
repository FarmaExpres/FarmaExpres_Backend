const { Router } = require("express");
const batchReportsController = require("../controllers/batchReportsController");
const { requireReportsRole } = require("../middlewares/reportsAccessMiddleware");

const router = Router();

router.get("/api/reports/alerts-batches", requireReportsRole, batchReportsController.getAlertsBatchesReport);

module.exports = router;
