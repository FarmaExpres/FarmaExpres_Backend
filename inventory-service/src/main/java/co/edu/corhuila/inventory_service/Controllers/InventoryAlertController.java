package co.edu.corhuila.inventory_service.Controllers;

import co.edu.corhuila.inventory_service.Dto.InventoryAlertBatchItemResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryBatchReportItemResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryExpiringProductResponse;
import co.edu.corhuila.inventory_service.Service.BatchService;
import co.edu.corhuila.inventory_service.Service.InventoryAlertQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InventoryAlertController {
    private final InventoryAlertQueryService inventoryAlertQueryService;
    private final BatchService batchService;

    public InventoryAlertController(InventoryAlertQueryService inventoryAlertQueryService, BatchService batchService) {
        this.inventoryAlertQueryService = inventoryAlertQueryService;
        this.batchService = batchService;
    }

    @GetMapping("/api/inventory/alerts/low-stock")
    public ResponseEntity<List<InventoryAlertBatchItemResponse>> listLowStockBatches() {
        return ResponseEntity.ok(inventoryAlertQueryService.listLowStockBatches());
    }

    @GetMapping("/api/inventory/alerts/out-of-stock")
    public ResponseEntity<List<InventoryAlertBatchItemResponse>> listOutOfStockBatches() {
        return ResponseEntity.ok(inventoryAlertQueryService.listOutOfStockBatches());
    }

    @GetMapping("/api/inventory/alerts/expired")
    public ResponseEntity<List<InventoryAlertBatchItemResponse>> listExpiredBatches() {
        return ResponseEntity.ok(inventoryAlertQueryService.listExpiredBatches());
    }

    @GetMapping("/api/inventory/alerts/expiring-soon")
    public ResponseEntity<List<InventoryAlertBatchItemResponse>> listExpiringBatches(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Boolean includeExpired,
            @RequestParam(required = false) Boolean onlyWithStock
    ) {
        return ResponseEntity.ok(
                inventoryAlertQueryService.listExpiringBatches(days, includeExpired, onlyWithStock)
        );
    }

    @GetMapping("/api/inventory/alerts/expiring-range")
    public ResponseEntity<List<InventoryExpiringProductResponse>> listProductsExpiringBetweenDays(
            @RequestParam Integer minDays,
            @RequestParam Integer maxDays
    ) {
        return ResponseEntity.ok(
                inventoryAlertQueryService.listProductsExpiringBetweenDays(minDays, maxDays)
        );
    }

    @GetMapping("/api/inventory/reports/expiring-products")
    public ResponseEntity<List<InventoryExpiringProductResponse>> listExpiringReportProducts(
            @RequestParam(required = false) Integer maxDays
    ) {
        return ResponseEntity.ok(inventoryAlertQueryService.listExpiringReportProducts(maxDays));
    }

    @GetMapping("/api/inventory/reports/batches")
    public ResponseEntity<List<InventoryBatchReportItemResponse>> listInventoryBatchesReport() {
        return ResponseEntity.ok(batchService.listInventoryBatchReport());
    }
}
