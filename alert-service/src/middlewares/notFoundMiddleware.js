function notFoundMiddleware(request, response) {
  return response.status(404).json({
    error: `Route ${request.method} ${request.originalUrl} not found`,
  });
}

module.exports = notFoundMiddleware;
