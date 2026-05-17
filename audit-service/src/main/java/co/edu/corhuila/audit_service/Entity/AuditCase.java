package co.edu.corhuila.audit_service.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "audit_case")
public class AuditCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long movementId;
    private Long productId;
    private Long batchId;
    private String movementType;
    private String medicineName;
    private Integer quantity;
    private String movementReason;
    private Long movementUserId;
    private String movementUserName;
    private String movementUserRole;
    private Instant movementDateTime;
    private BigDecimal riskScore = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    private AuditCaseStatus status = AuditCaseStatus.OPEN;
    @Enumerated(EnumType.STRING)
    private AuditCaseSource source;
    private String reason;
    @Enumerated(EnumType.STRING)
    private AuditPriority priority = AuditPriority.MEDIUM;
    private Long createdByUserId;
    private String createdByUserName;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Long closedByUserId;
    private String closedByUserName;
    private Instant closedAt;

    @OneToMany(mappedBy = "auditCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditObservation> observations = new ArrayList<>();

    @OneToMany(mappedBy = "auditCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditRuleResult> ruleResults = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getMovementId() { return movementId; }
    public void setMovementId(Long movementId) { this.movementId = movementId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getMovementReason() { return movementReason; }
    public void setMovementReason(String movementReason) { this.movementReason = movementReason; }
    public Long getMovementUserId() { return movementUserId; }
    public void setMovementUserId(Long movementUserId) { this.movementUserId = movementUserId; }
    public String getMovementUserName() { return movementUserName; }
    public void setMovementUserName(String movementUserName) { this.movementUserName = movementUserName; }
    public String getMovementUserRole() { return movementUserRole; }
    public void setMovementUserRole(String movementUserRole) { this.movementUserRole = movementUserRole; }
    public Instant getMovementDateTime() { return movementDateTime; }
    public void setMovementDateTime(Instant movementDateTime) { this.movementDateTime = movementDateTime; }
    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
    public AuditCaseStatus getStatus() { return status; }
    public void setStatus(AuditCaseStatus status) { this.status = status; }
    public AuditCaseSource getSource() { return source; }
    public void setSource(AuditCaseSource source) { this.source = source; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public AuditPriority getPriority() { return priority; }
    public void setPriority(AuditPriority priority) { this.priority = priority; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public String getCreatedByUserName() { return createdByUserName; }
    public void setCreatedByUserName(String createdByUserName) { this.createdByUserName = createdByUserName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getClosedByUserId() { return closedByUserId; }
    public void setClosedByUserId(Long closedByUserId) { this.closedByUserId = closedByUserId; }
    public String getClosedByUserName() { return closedByUserName; }
    public void setClosedByUserName(String closedByUserName) { this.closedByUserName = closedByUserName; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public List<AuditObservation> getObservations() { return observations; }
    public List<AuditRuleResult> getRuleResults() { return ruleResults; }

    public void addObservation(AuditObservation observation) {
        observations.add(observation);
        observation.setAuditCase(this);
    }

    public void addRuleResult(AuditRuleResult ruleResult) {
        ruleResults.add(ruleResult);
        ruleResult.setAuditCase(this);
    }
}
