package dev.springmind.wallet.wallet.dto;

import java.util.List;

public record TransactionsResponse(List<TransactionDto> items) {}
