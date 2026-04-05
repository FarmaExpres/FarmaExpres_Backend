package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class FefoConsumptionItemResponse {
    private Long batchId;
    private String batchCode;
    private LocalDate expirationDate;
    private Integer consumedQuantity;

    public FefoConsumptionItemResponse(Long batchId, String batchCode, LocalDate expirationDate, Integer consumedQuantity) {
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.expirationDate = expirationDate;
        this.consumedQuantity = consumedQuantity;
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

    public Integer getConsumedQuantity() {
        return consumedQuantity;
    }
}

