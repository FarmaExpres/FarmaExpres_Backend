package co.edu.corhuila.audit_service.Dto;

public record AuditRecalculateResponse(
        int processedMovements,
        int createdAutomaticCases,
        int synchronizedMovements
) {
}
