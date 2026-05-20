package co.edu.corhuila.audit_service.Dto;

import jakarta.validation.constraints.NotBlank;

public class AuditStatusUpdateRequest {
    @NotBlank
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
