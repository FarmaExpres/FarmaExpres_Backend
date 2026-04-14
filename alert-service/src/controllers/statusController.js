const statusService = require("../services/statusService");

async function getStatus(_request, response, next) {
  try {
    const payload = await statusService.buildStatus();
    const httpStatus = payload.status === "UP" ? 200 : 503;
    return response.status(httpStatus).json(payload);
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getStatus,
};
