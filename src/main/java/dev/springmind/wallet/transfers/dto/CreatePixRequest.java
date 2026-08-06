package dev.springmind.wallet.transfers.dto;

import dev.springmind.wallet.persistence.entity.PixKeyType;
import java.time.Instant;

public record CreatePixRequest(
        long amountCents,
        String beneficiaryId,
        String pixKey,
        PixKeyType pixKeyType,
        Instant scheduledFor) {}
