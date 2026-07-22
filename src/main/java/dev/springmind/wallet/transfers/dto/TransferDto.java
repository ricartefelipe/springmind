package dev.springmind.wallet.transfers.dto;

import java.time.Instant;

public record TransferDto(String id, String beneficiaryId, long amountCents, String status, Instant createdAt) {}
