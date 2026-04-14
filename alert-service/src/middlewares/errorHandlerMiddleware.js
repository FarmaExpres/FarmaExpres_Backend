function errorHandlerMiddleware(error, _request, response, _next) {
  const statusCode = error.statusCode || 500;
  const message = error.message || "Internal server error";

  return response.status(statusCode).json({
    error: message,
  });
}

module.exports = errorHandlerMiddleware;
