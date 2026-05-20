package co.edu.corhuila.audit_service.Controllers;

import co.edu.corhuila.audit_service.Dto.AuditHistoryResponse;
import co.edu.corhuila.audit_service.Dto.AuditInconsistencyResponse;
import co.edu.corhuila.audit_service.Dto.AuditMetricsResponse;
import co.edu.corhuila.audit_service.Dto.AuditNoteRequest;
import co.edu.corhuila.audit_service.Dto.AuditObservationResponse;
import co.edu.corhuila.audit_service.Dto.AuditRecalculateResponse;
import co.edu.corhuila.audit_service.Dto.AuditStatusUpdateRequest;
import co.edu.corhuila.audit_service.Dto.ManualAuditCaseRequest;
import co.edu.corhuila.audit_service.Dto.ManualAuditCaseResponse;
import co.edu.corhuila.audit_service.Service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<AuditHistoryResponse>> history(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(auditService.listHistory(authorizationHeader));
    }

    @GetMapping("/inconsistencies")
    public ResponseEntity<List<AuditInconsistencyResponse>> inconsistencies(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(auditService.listInconsistencies(authorizationHeader));
    }

    @GetMapping("/observations")
    public ResponseEntity<List<AuditObservationResponse>> observations() {
        return ResponseEntity.ok(auditService.listObservations());
    }

    @GetMapping("/metrics")
    public ResponseEntity<AuditMetricsResponse> metrics(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(auditService.getMetrics(authorizationHeader));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<AuditRecalculateResponse> recalculate(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(auditService.recalculate(authorizationHeader));
    }

    @PostMapping("/cases/manual")
    public ResponseEntity<ManualAuditCaseResponse> createManualCase(
            @Valid @RequestBody ManualAuditCaseRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(auditService.createManualCase(request, authorizationHeader));
    }

    @PatchMapping("/cases/{id}/note")
    public ResponseEntity<AuditObservationResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody AuditNoteRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(auditService.updateNote(id, request, authorizationHeader));
    }

    @PatchMapping("/cases/{id}/status")
    public ResponseEntity<ManualAuditCaseResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AuditStatusUpdateRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(auditService.updateStatus(id, request, authorizationHeader));
    }

    @DeleteMapping("/cases/{id}/manual-flag")
    public ResponseEntity<Void> deleteManualFlag(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        auditService.deleteManualFlag(id, authorizationHeader);
        return ResponseEntity.noContent().build();
    }
}
