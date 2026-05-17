package co.edu.corhuila.audit_service.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ManualAuditCaseRequest {
    @NotNull
    private Long movementId;
    @NotBlank
    private String note;
    private String priority;

    public Long getMovementId() { return movementId; }
    public void setMovementId(Long movementId) { this.movementId = movementId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
