package co.edu.corhuila.audit_service.Service;

import co.edu.corhuila.audit_service.Dto.AuditStatusUpdateRequest;
import co.edu.corhuila.audit_service.Dto.InventoryMovementResponse;
import co.edu.corhuila.audit_service.Dto.ManualAuditCaseRequest;
import co.edu.corhuila.audit_service.Entity.AuditCase;
import co.edu.corhuila.audit_service.Entity.AuditCaseSource;
import co.edu.corhuila.audit_service.Entity.AuditCaseStatus;
import co.edu.corhuila.audit_service.Entity.AuditPriority;
import co.edu.corhuila.audit_service.Repository.AuditCaseRepository;
import co.edu.corhuila.audit_service.Repository.AuditObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final String AUTH = "Bearer token";

    @Mock
    private AuditCaseRepository auditCaseRepository;
    @Mock
    private AuditObservationRepository auditObservationRepository;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private AuditRuleEngine auditRuleEngine;
    @InjectMocks
    private AuditService auditService;

    @Test
    void rejectsManualCaseWithoutNote() {
        ManualAuditCaseRequest request = new ManualAuditCaseRequest();
        request.setMovementId(21L);
        request.setNote(" ");

        assertThatThrownBy(() -> auditService.createManualCase(request, AUTH))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("La nota es obligatoria");
    }

    @Test
    void reusesOpenCaseAndSyncsMarkedWhenManualCaseAlreadyExists() {
        ManualAuditCaseRequest request = new ManualAuditCaseRequest();
        request.setMovementId(21L);
        request.setNote("Revisar salida");
        request.setPriority("HIGH");

        AuditCase existing = auditCase(21L, AuditCaseSource.AUTO, AuditCaseStatus.OPEN);
        when(inventoryClient.getMovement(21L, AUTH)).thenReturn(movement(21L));
        when(auditCaseRepository.findFirstByMovementIdOrderByCreatedAtDesc(21L)).thenReturn(Optional.of(existing));
        when(auditCaseRepository.save(existing)).thenReturn(existing);

        auditService.createManualCase(request, AUTH);

        assertThat(existing.getSource()).isEqualTo(AuditCaseSource.MANUAL);
        assertThat(existing.getPriority()).isEqualTo(AuditPriority.HIGH);
        assertThat(existing.getObservations()).hasSize(1);
        verify(inventoryClient).updateAuditStatus(21L, "MARKED", "Revisar salida", AUTH);
    }

    @Test
    void syncsReviewedWhenClosingCase() {
        AuditStatusUpdateRequest request = new AuditStatusUpdateRequest();
        request.setStatus("CLOSED");
        AuditCase existing = auditCase(21L, AuditCaseSource.MANUAL, AuditCaseStatus.OPEN);

        when(auditCaseRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(auditCaseRepository.save(existing)).thenReturn(existing);

        auditService.updateStatus(5L, request, AUTH);

        assertThat(existing.getStatus()).isEqualTo(AuditCaseStatus.CLOSED);
        verify(inventoryClient).updateAuditStatus(21L, "REVIEWED", "Marcado manual", AUTH);
    }

    @Test
    void recalculateCreatesOnlyMissingAutomaticCases() {
        InventoryMovementResponse movement = movement(21L);
        AuditRuleEngine.RuleFinding finding = new AuditRuleEngine.RuleFinding(
                "HIGH_QUANTITY_BY_MEDICINE_PATTERN",
                "Cantidad fuera del patron",
                "Mediana historica: 10",
                "Cantidad: 80",
                java.math.BigDecimal.valueOf(82.5),
                AuditPriority.HIGH,
                "Cantidad superior al patron historico"
        );

        when(inventoryClient.listMovements(AUTH)).thenReturn(List.of(movement));
        when(auditCaseRepository.findFirstByMovementIdOrderByCreatedAtDesc(21L)).thenReturn(Optional.empty());
        when(auditRuleEngine.evaluate(movement, List.of(movement))).thenReturn(List.of(finding));
        when(auditRuleEngine.strongest(List.of(finding))).thenReturn(Optional.of(finding));
        when(auditCaseRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.recalculate(AUTH);

        ArgumentCaptor<List<AuditCase>> captor = ArgumentCaptor.forClass(List.class);
        verify(auditCaseRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        verify(inventoryClient).updateAuditStatus(21L, "MARKED", "Cantidad superior al patron historico", AUTH);
    }

    private AuditCase auditCase(Long movementId, AuditCaseSource source, AuditCaseStatus status) {
        AuditCase auditCase = new AuditCase();
        auditCase.setMovementId(movementId);
        auditCase.setSource(source);
        auditCase.setStatus(status);
        auditCase.setReason(source == AuditCaseSource.MANUAL ? "Marcado manual" : "Automatico");
        auditCase.setPriority(AuditPriority.MEDIUM);
        auditCase.setQuantity(75);
        auditCase.setMovementType("Exit");
        auditCase.setMedicineName("Losartan 50 mg");
        return auditCase;
    }

    private InventoryMovementResponse movement(Long id) {
        InventoryMovementResponse response = new InventoryMovementResponse();
        response.setId(id);
        response.setProductId(1L);
        response.setProductName("Losartan 50 mg");
        response.setType("Exit");
        response.setAmount(75);
        response.setReason("Dispensacion");
        response.setDateTime(Instant.parse("2026-05-11T15:00:00Z"));
        response.setUserName("Marlon Romero");
        return response;
    }
}
