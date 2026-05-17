package co.edu.corhuila.audit_service.Dto;

import java.math.BigDecimal;

public record AuditHistoryResponse(
        Long id,
        Long movementId,
        String date,
        String time,
        String type,
        String medicine,
        Integer quantity,
        Integer absoluteQuantity,
        String user,
        String reason,
        String auditStatus,
        String auditSource,
        String auditPriority,
        String auditReason,
        String auditNote,
        BigDecimal riskScore
) {
}
