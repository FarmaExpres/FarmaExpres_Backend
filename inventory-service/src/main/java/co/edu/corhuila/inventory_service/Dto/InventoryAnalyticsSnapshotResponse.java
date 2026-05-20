package co.edu.corhuila.inventory_service.Dto;

import java.time.Instant;
import java.util.List;

public record InventoryAnalyticsSnapshotResponse(
        Instant generatedAt,
        List<InventoryAnalyticsProductResponse> products,
        List<InventoryAnalyticsBatchResponse> batches,
        List<InventoryAnalyticsMovementResponse> movements
) {
}
