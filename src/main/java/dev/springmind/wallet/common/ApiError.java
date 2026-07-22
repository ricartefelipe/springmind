package dev.springmind.wallet.common;

public record ApiError(String code, String message, String correlationId) {}
