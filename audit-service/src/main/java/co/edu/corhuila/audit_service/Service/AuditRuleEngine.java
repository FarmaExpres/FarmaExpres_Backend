package co.edu.corhuila.audit_service.Service;

import co.edu.corhuila.audit_service.Dto.InventoryMovementResponse;
import co.edu.corhuila.audit_service.Entity.AuditPriority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuditRuleEngine {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    public List<RuleFinding> evaluate(InventoryMovementResponse movement, List<InventoryMovementResponse> history) {
        return List.of(
                        highQuantityPattern(movement, history),
                        repeatedLossReason(movement, history),
                        outOfHoursMovement(movement)
                )
                .stream()
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<RuleFinding> highQuantityPattern(InventoryMovementResponse movement, List<InventoryMovementResponse> history) {
        if (movement.getAmount() == null || movement.getProductId() == null || movement.getType() == null) {
            return Optional.empty();
        }

        List<Integer> comparableAmounts = history.stream()
                .filter(item -> item.getProductId() != null && item.getProductId().equals(movement.getProductId()))
                .filter(item -> item.getType() != null && item.getType().equalsIgnoreCase(movement.getType()))
                .filter(item -> item.getAmount() != null && item.getId() != null && !item.getId().equals(movement.getId()))
                .map(InventoryMovementResponse::getAmount)
                .map(Math::abs)
                .sorted()
                .toList();

        int amount = Math.abs(movement.getAmount());
        if (comparableAmounts.size() < 3) {
            int historicalMax = comparableAmounts.stream().mapToInt(Integer::intValue).max().orElse(0);
            int threshold = Math.max(100, Math.max(historicalMax * 2, historicalMax + 50));
            if (amount <= threshold) {
                return Optional.empty();
            }

            return Optional.of(new RuleFinding(
                    "HIGH_QUANTITY_WITH_LOW_HISTORY",
                    "Cantidad alta con poco historial",
                    "Umbral inicial: " + threshold,
                    "Cantidad: " + amount,
                    BigDecimal.valueOf(78),
                    AuditPriority.HIGH,
                    "Cantidad alta para un medicamento con poco historial comparable"
            ));
        }

        int median = comparableAmounts.get(comparableAmounts.size() / 2);
        int threshold = Math.max(median * 3, median + 20);
        if (amount <= threshold) {
            return Optional.empty();
        }

        return Optional.of(new RuleFinding(
                "HIGH_QUANTITY_BY_MEDICINE_PATTERN",
                "Cantidad fuera del patron del medicamento",
                "Mediana historica: " + median,
                "Cantidad: " + amount,
                BigDecimal.valueOf(82.5),
                AuditPriority.HIGH,
                "Cantidad superior al patron historico del medicamento"
        ));
    }

    private Optional<RuleFinding> repeatedLossReason(InventoryMovementResponse movement, List<InventoryMovementResponse> history) {
        String reason = movement.getReason() == null ? "" : movement.getReason().toLowerCase(Locale.ROOT);
        if (!reason.contains("merma")) {
            return Optional.empty();
        }

        long repeated = history.stream()
                .filter(item -> item.getUserId() != null && item.getUserId().equals(movement.getUserId()))
                .filter(item -> item.getReason() != null && item.getReason().toLowerCase(Locale.ROOT).contains("merma"))
                .count();

        if (repeated < 3) {
            return Optional.empty();
        }

        return Optional.of(new RuleFinding(
                "REPEATED_LOSS_REASON",
                "Mermas repetidas",
                "Menos de 3 mermas por usuario",
                "Mermas detectadas: " + repeated,
                BigDecimal.valueOf(70),
                AuditPriority.MEDIUM,
                "Mermas repetidas para el mismo usuario"
        ));
    }

    private Optional<RuleFinding> outOfHoursMovement(InventoryMovementResponse movement) {
        if (movement.getDateTime() == null) {
            return Optional.empty();
        }

        int hour = movement.getDateTime().atZone(BOGOTA_ZONE).getHour();
        if (hour >= 6 && hour <= 22) {
            return Optional.empty();
        }

        return Optional.of(new RuleFinding(
                "OUT_OF_HOURS_MOVEMENT",
                "Movimiento fuera de horario",
                "Horario operativo esperado: 06:00-22:59 America/Bogota",
                "Hora America/Bogota: " + hour,
                BigDecimal.valueOf(55),
                AuditPriority.MEDIUM,
                "Movimiento registrado fuera del horario habitual"
        ));
    }

    public Optional<RuleFinding> strongest(List<RuleFinding> findings) {
        return findings.stream().max(Comparator.comparing(RuleFinding::score));
    }

    public record RuleFinding(
            String ruleCode,
            String ruleName,
            String expectedValue,
            String actualValue,
            BigDecimal score,
            AuditPriority priority,
            String reason
    ) {
    }
}
