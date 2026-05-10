package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class InventoryAlertBatchItemResponse {
    private final Long productId;
    private final String productCode;
    private final String productName;
    private final Integer minimumStock;
    private final Long batchId;
    private final String batchCode;
    private final LocalDate expirationDate;
    private final Integer availableStock;
    private final Integer batchStock;
    private final Integer expiredBatchStock;
    private final Integer operationalStock;
    private final String status;

    public InventoryAlertBatchItemResponse(
            Long productId,
            String productCode,
            String productName,
            Integer minimumStock,
            Long batchId,
            String batchCode,
            LocalDate expirationDate,
            Integer availableStock,
            Integer batchStock,
            Integer expiredBatchStock,
            Integer operationalStock,
            String status
    ) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.minimumStock = minimumStock;
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.expirationDate = expirationDate;
        this.availableStock = availableStock;
        this.batchStock = batchStock;
        this.expiredBatchStock = expiredBatchStock;
        this.operationalStock = operationalStock;
        this.status = status;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getBatchStock() {
        return batchStock;
    }

    public Integer getExpiredBatchStock() {
        return expiredBatchStock;
    }

    public Integer getOperationalStock() {
        return operationalStock;
    }

    public String getStatus() {
        return status;
    }
}
