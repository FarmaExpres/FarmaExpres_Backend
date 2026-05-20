package co.edu.corhuila.audit_service.Dto;

import java.time.Instant;

public record AuditObservationResponse(
        Long id,
        Long auditCaseId,
        Long movementId,
        String priority,
        String description,
        String user,
        String createdBy,
        Instant createdAt
) {
}
