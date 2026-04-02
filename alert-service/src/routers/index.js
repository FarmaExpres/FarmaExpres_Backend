const { Router } = require("express");
const alertRoutes = require("./alertRoutes");
const statusRoutes = require("./statusRoutes");

const router = Router();

router.use(alertRoutes);
router.use(statusRoutes);

module.exports = router;
