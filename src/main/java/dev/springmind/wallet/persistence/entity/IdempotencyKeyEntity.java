package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

    @Id
    @Column(name = "key_value")
    private String keyValue;

    @Column(name = "transfer_id", nullable = false)
    private String transferId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKeyEntity() {}

    public IdempotencyKeyEntity(String keyValue, String transferId, Instant createdAt) {
        this.keyValue = keyValue;
        this.transferId = transferId;
        this.createdAt = createdAt;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getTransferId() {
        return transferId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
