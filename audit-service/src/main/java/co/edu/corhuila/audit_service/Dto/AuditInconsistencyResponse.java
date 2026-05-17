package co.edu.corhuila.audit_service.Dto;

import java.math.BigDecimal;

public record AuditInconsistencyResponse(
        Long id,
        Long movementId,
        String medicine,
        String type,
        Integer quantity,
        String user,
        String reason,
        String priority,
        String source,
        BigDecimal riskScore,
        String status
) {
}
