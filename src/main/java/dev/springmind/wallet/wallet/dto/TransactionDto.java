package dev.springmind.wallet.wallet.dto;

import dev.springmind.wallet.persistence.entity.TransactionType;
import java.time.Instant;

public record TransactionDto(
        String id,
        TransactionType type,
        long amountCents,
        String description,
        Instant createdAt,
        String counterparty) {}
