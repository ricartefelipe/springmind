package dev.springmind.wallet.transfers.dto;

public record CreatePixRequest(String beneficiaryId, long amountCents) {}
