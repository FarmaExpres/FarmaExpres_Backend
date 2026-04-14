const { Router } = require("express");
const alertRoutes = require("./alertRoutes");
const reportRoutes = require("./reportRoutes");
const statusRoutes = require("./statusRoutes");

const router = Router();

router.use(alertRoutes);
router.use(reportRoutes);
router.use(statusRoutes);

module.exports = router;
