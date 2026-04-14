
package co.edu.corhuila.auth_service.Entity;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "binnacle")
public class Binnacle {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Instant dateTime;

    public Binnacle() {}

    public Binnacle(Long userId, String action) {
        this.userId = userId;
        this.action = action;
        this.dateTime = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }
}
