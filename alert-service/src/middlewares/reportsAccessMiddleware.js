const REPORT_ALLOWED_ROLES = new Set(["ADMIN", "AUDITOR", "FARMACEUTICO"]);

function decodeJwtPayload(token) {
  const parts = token.split(".");
  if (parts.length < 2 || !parts[1]) {
    throw new Error("Malformed JWT");
  }

  const normalized = parts[1].replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);
  const decoded = Buffer.from(padded, "base64").toString("utf8");
  return JSON.parse(decoded);
}

function requireReportsRole(request, response, next) {
  const authHeader = request.get("Authorization");
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return response.status(401).json({ error: "UNAUTHORIZED", message: "Token requerido" });
  }

  try {
    const token = authHeader.substring(7).trim();
    const payload = decodeJwtPayload(token);
    const role = String(payload.rol || payload.role || "").toUpperCase();

    const exp = Number(payload.exp);
    if (Number.isFinite(exp)) {
      const nowInSeconds = Math.floor(Date.now() / 1000);
      if (exp <= nowInSeconds) {
        return response.status(401).json({ error: "UNAUTHORIZED", message: "Token expirado" });
      }
    }

    if (!REPORT_ALLOWED_ROLES.has(role)) {
      return response.status(403).json({ error: "FORBIDDEN", message: "Acceso denegado para este rol" });
    }

    return next();
  } catch (_error) {
    return response.status(401).json({ error: "UNAUTHORIZED", message: "Token invalido" });
  }
}

module.exports = {
  requireReportsRole,
};
