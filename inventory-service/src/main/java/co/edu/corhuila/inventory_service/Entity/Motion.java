package co.edu.corhuila.inventory_service.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "motion")
public class Motion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MovementType Type;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MotionStatus status = MotionStatus.NORMAL;

    @Column(name = "marked_by_user_id")
    private Long markedByUserId;

    @Column(name = "marked_by_user_name")
    private String markedByUserName;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "observation")
    private String observation;

    @ManyToOne
    @JoinColumn(name = "produc_id")
    @JsonIgnore
    private Product product;

    public Motion() {}

    public Motion(MovementType Type,
                  Integer amount,
                  Product product) {
        this(Type, amount, product, null, null, null, null, null);
    }

    public Motion(MovementType Type,
                  Integer amount,
                  Product product,
                  String reason,
                  Long userId,
                  String userName,
                  String userEmail,
                  String userRole) {
        this.Type = Type;
        this.amount = amount;
        this.product = product;
        this.dateTime = LocalDateTime.now();
        this.reason = reason;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.status = MotionStatus.NORMAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getType() {
        return Type;
    }

    public void setType(MovementType type) {
        Type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public MotionStatus getStatus() {
        return status;
    }

    public void setStatus(MotionStatus status) {
        this.status = status;
    }

    public Long getMarkedByUserId() {
        return markedByUserId;
    }

    public void setMarkedByUserId(Long markedByUserId) {
        this.markedByUserId = markedByUserId;
    }

    public String getMarkedByUserName() {
        return markedByUserName;
    }

    public void setMarkedByUserName(String markedByUserName) {
        this.markedByUserName = markedByUserName;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}


