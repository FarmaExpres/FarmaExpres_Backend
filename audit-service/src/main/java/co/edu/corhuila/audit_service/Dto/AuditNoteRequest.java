package co.edu.corhuila.audit_service.Dto;

import jakarta.validation.constraints.NotBlank;

public class AuditNoteRequest {
    @NotBlank
    private String note;
    private String priority;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
