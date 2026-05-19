package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public record InventoryAnalyticsBatchResponse(
        Long batchId,
        Long productId,
        String batchCode,
        LocalDate expirationDate,
        Integer initialStock,
        Integer availableStock,
        String status
) {
}
