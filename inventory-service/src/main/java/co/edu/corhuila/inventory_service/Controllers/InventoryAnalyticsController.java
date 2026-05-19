package co.edu.corhuila.inventory_service.Controllers;

import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsSnapshotResponse;
import co.edu.corhuila.inventory_service.Service.InventoryAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryAnalyticsController {
    private final InventoryAnalyticsService inventoryAnalyticsService;

    public InventoryAnalyticsController(InventoryAnalyticsService inventoryAnalyticsService) {
        this.inventoryAnalyticsService = inventoryAnalyticsService;
    }

    @GetMapping("/api/inventory/analytics/snapshot")
    public ResponseEntity<InventoryAnalyticsSnapshotResponse> getAnalyticsSnapshot() {
        return ResponseEntity.ok(inventoryAnalyticsService.buildSnapshot());
    }
}
