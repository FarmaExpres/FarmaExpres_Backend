package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class InventoryExpiringProductResponse {
    private final Long id;
    private final String code;
    private final String name;
    private final Integer stock;
    private final Integer minimumStock;
    private final LocalDate expirationDate;
    private final Boolean active;

    public InventoryExpiringProductResponse(
            Long id,
            String code,
            String name,
            Integer stock,
            Integer minimumStock,
            LocalDate expirationDate,
            Boolean active
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.expirationDate = expirationDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Boolean getActive() {
        return active;
    }
}
