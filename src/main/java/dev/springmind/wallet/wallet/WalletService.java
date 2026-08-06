package dev.springmind.wallet.wallet;

import dev.springmind.wallet.onboarding.OnboardingService;
import dev.springmind.wallet.persistence.entity.AccountEntity;
import dev.springmind.wallet.persistence.entity.OnboardingStepCode;
import dev.springmind.wallet.persistence.entity.TransactionEntity;
import dev.springmind.wallet.persistence.entity.TransactionType;
import dev.springmind.wallet.persistence.repository.AccountRepository;
import dev.springmind.wallet.persistence.repository.TransactionRepository;
import dev.springmind.wallet.transfers.PixService;
import dev.springmind.wallet.wallet.dto.BalanceResponse;
import dev.springmind.wallet.wallet.dto.TransactionDto;
import dev.springmind.wallet.wallet.dto.TransactionsResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final String DEMO_USER_ID = "u1";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PixService pixService;
    private final OnboardingService onboardingService;

    public WalletService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PixService pixService,
            OnboardingService onboardingService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.pixService = pixService;
        this.onboardingService = onboardingService;
    }

    @Transactional
    public BalanceResponse getBalance() {
        pixService.processDueScheduledTransfers();
        AccountEntity account = accountRepository
                .findByUserId(DEMO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Conta demo não encontrada."));
        resetDailySpentIfNeeded(account);
        accountRepository.save(account);
        return new BalanceResponse(
                account.getAvailableCents(),
                account.getBlockedCents(),
                account.getDailyLimitCents(),
                account.getDailySpentCents(),
                account.getCurrency());
    }

    @Transactional
    public TransactionsResponse getTransactions(
            Instant from, Instant to, String type, String q, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        TransactionType typeFilter = parseType(type);
        String query = q == null ? "" : q.trim().toLowerCase();

        List<TransactionDto> filtered = transactionRepository.findAll().stream()
                .filter(transaction -> from == null || !transaction.getCreatedAt().isBefore(from))
                .filter(transaction -> to == null || !transaction.getCreatedAt().isAfter(to))
                .filter(transaction -> typeFilter == null || transaction.getType() == typeFilter)
                .filter(transaction -> query.isEmpty()
                        || transaction.getDescription().toLowerCase().contains(query)
                        || transaction.getCounterparty().toLowerCase().contains(query))
                .sorted(Comparator.comparing(TransactionEntity::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();

        long total = filtered.size();
        int start = (safePage - 1) * safePageSize;
        List<TransactionDto> items = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, Math.min(start + safePageSize, filtered.size()));

        onboardingService.markDone(OnboardingStepCode.VIEW_STATEMENT);
        return new TransactionsResponse(items, safePage, safePageSize, total);
    }

    public static void resetDailySpentIfNeeded(AccountEntity account) {
        LocalDate today = LocalDate.now();
        if (account.getDailySpentOn() == null || !account.getDailySpentOn().equals(today)) {
            account.setDailySpentCents(0);
            account.setDailySpentOn(today);
        }
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
