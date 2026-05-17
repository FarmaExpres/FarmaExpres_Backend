package co.edu.corhuila.audit_service.Dto;

public record ManualAuditCaseResponse(
        Long id,
        Long movementId,
        String status,
        String source,
        String priority,
        String reason
) {
}
