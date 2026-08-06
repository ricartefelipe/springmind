package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    private String id;

    @Column(name = "beneficiary_id")
    private String beneficiaryId;

    @Column(name = "pix_key")
    private String pixKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "pix_key_type")
    private PixKeyType pixKeyType;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "scheduled_for")
    private Instant scheduledFor;

    @Column(name = "end_to_end_id")
    private String endToEndId;

    @Column(name = "correlation_id")
    private String correlationId;

    protected TransferEntity() {}

    public TransferEntity(String id, String beneficiaryId, long amountCents, String status, Instant createdAt) {
        this(
                id,
                beneficiaryId,
                null,
                null,
                amountCents,
                TransferStatus.valueOf(status),
                createdAt,
                null,
                null,
                null);
    }

    public TransferEntity(
            String id,
            String beneficiaryId,
            String pixKey,
            PixKeyType pixKeyType,
            long amountCents,
            TransferStatus status,
            Instant createdAt,
            Instant scheduledFor,
            String endToEndId,
            String correlationId) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.pixKey = pixKey;
        this.pixKeyType = pixKeyType;
        this.amountCents = amountCents;
        this.status = status;
        this.createdAt = createdAt;
        this.scheduledFor = scheduledFor;
        this.endToEndId = endToEndId;
        this.correlationId = correlationId;
    }

    public String getId() {
        return id;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public String getPixKey() {
        return pixKey;
    }

    public PixKeyType getPixKeyType() {
        return pixKeyType;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
