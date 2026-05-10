package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.InventoryAlertBatchItemResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryExpiringProductResponse;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryAlertQueryService {
    private static final int DEFAULT_EXPIRING_SOON_DAYS = 15;
    private static final int DEFAULT_MAX_REPORT_DAYS = 60;

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;

    public InventoryAlertQueryService(BatchRepository batchRepository, ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    public List<InventoryAlertBatchItemResponse> listExpiredBatches() {
        return batchRepository.findExpiredAlertBatches().stream()
                .map(this::toBatchResponse)
                .toList();
    }

    public List<InventoryAlertBatchItemResponse> listExpiringBatches(
            Integer days,
            Boolean includeExpired,
            Boolean onlyWithStock
    ) {
        int daysWindow = days != null && days >= 0 ? days : DEFAULT_EXPIRING_SOON_DAYS;
        return batchRepository.findExpiringAlertBatches(
                        daysWindow,
                        Boolean.TRUE.equals(includeExpired),
                        Boolean.TRUE.equals(onlyWithStock)
                )
                .stream()
                .map(this::toBatchResponse)
                .toList();
    }

    public List<InventoryAlertBatchItemResponse> listLowStockBatches() {
        return batchRepository.findLowStockAlertBatches().stream()
                .map(this::toBatchResponse)
                .toList();
    }

    public List<InventoryAlertBatchItemResponse> listOutOfStockBatches() {
        return batchRepository.findOutOfStockAlertBatches().stream()
                .map(this::toBatchResponse)
                .toList();
    }

    public List<InventoryExpiringProductResponse> listProductsExpiringBetweenDays(Integer minDays, Integer maxDays) {
        int safeMinDays = minDays != null && minDays >= 0 ? minDays : 0;
        int safeMaxDays = maxDays != null && maxDays >= safeMinDays ? maxDays : safeMinDays;

        return productRepository.findProductsExpiringBetweenDays(safeMinDays, safeMaxDays).stream()
                .map(this::toProductResponse)
                .toList();
    }

    public List<InventoryExpiringProductResponse> listExpiringReportProducts(Integer maxDays) {
        int maxDaysWindow = maxDays != null && maxDays >= 0 ? maxDays : DEFAULT_MAX_REPORT_DAYS;
        return productRepository.findExpiringReportProducts(maxDaysWindow).stream()
                .map(this::toProductResponse)
                .toList();
    }

    private InventoryAlertBatchItemResponse toBatchResponse(BatchRepository.InventoryAlertBatchProjection row) {
        return new InventoryAlertBatchItemResponse(
                row.getProductId(),
                row.getProductCode(),
                row.getProductName(),
                row.getMinimumStock(),
                row.getBatchId(),
                row.getBatchCode(),
                row.getExpirationDate(),
                row.getAvailableStock(),
                row.getBatchStock(),
                row.getExpiredBatchStock(),
                row.getOperationalStock(),
                row.getStatus()
        );
    }

    private InventoryExpiringProductResponse toProductResponse(ProductRepository.InventoryExpiringProductProjection row) {
        return new InventoryExpiringProductResponse(
                row.getId(),
                row.getCode(),
                row.getName(),
                row.getStock(),
                row.getMinimumStock(),
                row.getExpirationDate(),
                row.getActive()
        );
    }
}
