package co.edu.corhuila.inventory_service.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryAnalyticsProductResponse(
        Long productId,
        String productCode,
        String productName,
        String genericName,
        String concentration,
        String dosageForm,
        String presentation,
        String category,
        Integer currentStock,
        Integer minimumStock,
        Integer maximumStock,
        BigDecimal unitPrice,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        LocalDate expirationDate,
        Boolean active
) {
}
