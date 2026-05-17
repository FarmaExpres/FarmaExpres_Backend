package co.edu.corhuila.audit_service.Dto;

import java.time.Instant;
import java.time.LocalDate;

public class InventoryMovementResponse {
    private Long id;
    private Instant dateTime;
    private String type;
    private Integer amount;
    private Long productId;
    private String productName;
    private Long batchId;
    private String batchCode;
    private LocalDate batchExpirationDate;
    private String reason;
    private Long userId;
    private String userName;
    private String userRole;
    private String observation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getDateTime() { return dateTime; }
    public void setDateTime(Instant dateTime) { this.dateTime = dateTime; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public LocalDate getBatchExpirationDate() { return batchExpirationDate; }
    public void setBatchExpirationDate(LocalDate batchExpirationDate) { this.batchExpirationDate = batchExpirationDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
