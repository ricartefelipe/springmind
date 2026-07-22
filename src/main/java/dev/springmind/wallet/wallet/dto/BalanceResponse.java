package dev.springmind.wallet.wallet.dto;

public record BalanceResponse(long availableCents, String currency) {}
