package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "available_cents", nullable = false)
    private long availableCents;

    @Column(nullable = false)
    private String currency;

    protected AccountEntity() {}

    public AccountEntity(String id, String userId, long availableCents, String currency) {
        this.id = id;
        this.userId = userId;
        this.availableCents = availableCents;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getAvailableCents() {
        return availableCents;
    }

    public void setAvailableCents(long availableCents) {
        this.availableCents = availableCents;
    }

    public String getCurrency() {
        return currency;
    }
}
