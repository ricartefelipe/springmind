package dev.springmind.wallet.wallet;

import dev.springmind.wallet.persistence.entity.AccountEntity;
import dev.springmind.wallet.persistence.entity.TransactionEntity;
import dev.springmind.wallet.persistence.entity.TransactionType;
import dev.springmind.wallet.persistence.repository.AccountRepository;
import dev.springmind.wallet.persistence.repository.TransactionRepository;
import dev.springmind.wallet.wallet.dto.BalanceResponse;
import dev.springmind.wallet.wallet.dto.TransactionDto;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private static final String DEMO_USER_ID = "u1";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public BalanceResponse getBalance() {
        AccountEntity account = accountRepository.findByUserId(DEMO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Conta demo não encontrada."));
        return new BalanceResponse(account.getAvailableCents(), account.getCurrency());
    }

    public List<TransactionDto> getTransactions(Instant from, Instant to, String type) {
        TransactionType typeFilter = parseType(type);
        return transactionRepository.findAll().stream()
                .filter(transaction -> from == null || !transaction.getCreatedAt().isBefore(from))
                .filter(transaction -> to == null || !transaction.getCreatedAt().isAfter(to))
                .filter(transaction -> typeFilter == null || transaction.getType() == typeFilter)
                .sorted(Comparator.comparing(TransactionEntity::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();
    }

    private TransactionType parseType(String type) {
        if (type == null || type.isBlank() || type.equalsIgnoreCase("ALL")) {
            return null;
        }
        return TransactionType.valueOf(type);
    }

    private TransactionDto toDto(TransactionEntity transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmountCents(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCounterparty());
    }
}
