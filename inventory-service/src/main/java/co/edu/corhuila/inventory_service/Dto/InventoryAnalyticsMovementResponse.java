package co.edu.corhuila.inventory_service.Dto;

import java.time.Instant;
import java.time.LocalDate;

public record InventoryAnalyticsMovementResponse(
        Long movementId,
        Long productId,
        String productCode,
        String productName,
        Long batchId,
        String batchCode,
        String movementType,
        Integer amount,
        Instant movementDate,
        String reason,
        Long userId,
        String userName,
        String userEmail,
        String userRole,
        Integer stock,
        Integer minimumStock,
        LocalDate expirationDate,
        LocalDate batchExpirationDate
) {
}
