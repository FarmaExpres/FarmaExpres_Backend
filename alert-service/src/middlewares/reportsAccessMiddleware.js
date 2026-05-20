const crypto = require("crypto");

const REPORT_ALLOWED_ROLES = new Set(["ADMIN", "AUDITOR", "FARMACEUTICO"]);

function base64UrlDecode(value) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);
  return Buffer.from(padded, "base64");
}

function verifyJwt(token) {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error("JWT_SECRET is not configured");
  }

  const parts = token.split(".");
  if (parts.length !== 3 || !parts[0] || !parts[1] || !parts[2]) {
    throw new Error("Malformed JWT");
  }

  const signedContent = `${parts[0]}.${parts[1]}`;
  const expectedSignature = crypto
    .createHmac("sha256", secret)
    .update(signedContent)
    .digest("base64url");

  const expectedBuffer = Buffer.from(expectedSignature);
  const actualBuffer = Buffer.from(parts[2]);
  if (
    expectedBuffer.length !== actualBuffer.length ||
    !crypto.timingSafeEqual(expectedBuffer, actualBuffer)
  ) {
    throw new Error("Invalid JWT signature");
  }

  return JSON.parse(base64UrlDecode(parts[1]).toString("utf8"));
}

function requireReportsRole(request, response, next) {
  const payload = authenticateRequest(request, response);
  if (!payload) {
    return undefined;
  }

  const role = String(payload.rol || payload.role || "").toUpperCase();
  if (!REPORT_ALLOWED_ROLES.has(role)) {
    return response.status(403).json({ error: "FORBIDDEN", message: "Acceso denegado para este rol" });
  }

  return next();
}

function requireAuthenticated(request, response, next) {
  const payload = authenticateRequest(request, response);
  if (!payload) {
    return undefined;
  }

  return next();
}

function authenticateRequest(request, response) {
  const authHeader = request.get("Authorization");
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    response.status(401).json({ error: "UNAUTHORIZED", message: "Token requerido" });
    return null;
  }

  try {
    const token = authHeader.substring(7).trim();
    const payload = verifyJwt(token);

    const exp = Number(payload.exp);
    if (Number.isFinite(exp)) {
      const nowInSeconds = Math.floor(Date.now() / 1000);
      if (exp <= nowInSeconds) {
        response.status(401).json({ error: "UNAUTHORIZED", message: "Token expirado" });
        return null;
      }
    }

    return payload;
  } catch (_error) {
    response.status(401).json({ error: "UNAUTHORIZED", message: "Token invalido" });
    return null;
  }
}

module.exports = {
  requireAuthenticated,
  requireReportsRole,
};
