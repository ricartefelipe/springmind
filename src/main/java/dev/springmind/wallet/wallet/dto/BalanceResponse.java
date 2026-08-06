package dev.springmind.wallet.wallet.dto;

public record BalanceResponse(
        long availableCents,
        long blockedCents,
        long dailyLimitCents,
        long dailySpentCents,
        String currency) {}
