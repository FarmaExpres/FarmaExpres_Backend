const express = require("express");
const { env } = require("../config/env");
const routes = require("../routers");
const notFoundMiddleware = require("../middlewares/notFoundMiddleware");
const errorHandlerMiddleware = require("../middlewares/errorHandlerMiddleware");

const app = express();

app.use(express.json());
app.use(routes);
app.use(notFoundMiddleware);
app.use(errorHandlerMiddleware);

module.exports = {
  app,
  env,
};
