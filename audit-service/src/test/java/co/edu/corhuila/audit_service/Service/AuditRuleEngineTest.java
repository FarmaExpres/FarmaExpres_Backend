package co.edu.corhuila.audit_service.Service;

import co.edu.corhuila.audit_service.Dto.InventoryMovementResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditRuleEngineTest {

    private final AuditRuleEngine auditRuleEngine = new AuditRuleEngine();

    @Test
    void detectsQuantityOutsideMedicinePattern() {
        InventoryMovementResponse target = movement(10L, 1L, "Exit", 90);
        List<InventoryMovementResponse> history = new ArrayList<>(List.of(
                movement(1L, 1L, "Exit", 10),
                movement(2L, 1L, "Exit", 11),
                movement(3L, 1L, "Exit", 12),
                movement(4L, 1L, "Exit", 9),
                target
        ));

        List<AuditRuleEngine.RuleFinding> findings = auditRuleEngine.evaluate(target, history);

        assertThat(findings)
                .extracting(AuditRuleEngine.RuleFinding::ruleCode)
                .contains("HIGH_QUANTITY_BY_MEDICINE_PATTERN");
    }

    @Test
    void doesNotFlagNormalQuantityWithSmallHistory() {
        InventoryMovementResponse target = movement(10L, 1L, "Exit", 14);
        List<InventoryMovementResponse> history = List.of(
                movement(1L, 1L, "Exit", 10),
                movement(2L, 1L, "Exit", 11),
                target
        );

        List<AuditRuleEngine.RuleFinding> findings = auditRuleEngine.evaluate(target, history);

        assertThat(findings).isEmpty();
    }

    private InventoryMovementResponse movement(Long id, Long productId, String type, Integer amount) {
        InventoryMovementResponse response = new InventoryMovementResponse();
        response.setId(id);
        response.setProductId(productId);
        response.setType(type);
        response.setAmount(amount);
        response.setDateTime(Instant.parse("2026-05-11T15:00:00Z"));
        response.setProductName("Losartan 50 mg");
        response.setUserName("Marlon Romero");
        return response;
    }
}
