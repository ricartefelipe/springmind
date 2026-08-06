package dev.springmind.wallet.transfers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.springmind.wallet.persistence.entity.PixKeyType;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferDto(
        String id,
        String beneficiaryId,
        String pixKey,
        PixKeyType pixKeyType,
        long amountCents,
        String status,
        Instant createdAt,
        Instant scheduledFor,
        String endToEndId,
        String correlationId) {}
