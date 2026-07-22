package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String counterparty;

    protected TransactionEntity() {}

    public TransactionEntity(
            String id,
            TransactionType type,
            long amountCents,
            String description,
            Instant createdAt,
            String counterparty) {
        this.id = id;
        this.type = type;
        this.amountCents = amountCents;
        this.description = description;
        this.createdAt = createdAt;
        this.counterparty = counterparty;
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCounterparty() {
        return counterparty;
    }
}
