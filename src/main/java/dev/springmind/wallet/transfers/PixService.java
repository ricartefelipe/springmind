package dev.springmind.wallet.transfers;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.common.CorrelationIds;
import dev.springmind.wallet.common.PixKeyValidator;
import dev.springmind.wallet.onboarding.OnboardingService;
import dev.springmind.wallet.persistence.entity.AccountEntity;
import dev.springmind.wallet.persistence.entity.BeneficiaryEntity;
import dev.springmind.wallet.persistence.entity.IdempotencyKeyEntity;
import dev.springmind.wallet.persistence.entity.NotificationEntity;
import dev.springmind.wallet.persistence.entity.OnboardingStepCode;
import dev.springmind.wallet.persistence.entity.PixKeyType;
import dev.springmind.wallet.persistence.entity.TransactionEntity;
import dev.springmind.wallet.persistence.entity.TransactionType;
import dev.springmind.wallet.persistence.entity.TransferEntity;
import dev.springmind.wallet.persistence.entity.TransferStatus;
import dev.springmind.wallet.persistence.repository.AccountRepository;
import dev.springmind.wallet.persistence.repository.BeneficiaryRepository;
import dev.springmind.wallet.persistence.repository.IdempotencyKeyRepository;
import dev.springmind.wallet.persistence.repository.NotificationRepository;
import dev.springmind.wallet.persistence.repository.TransactionRepository;
import dev.springmind.wallet.persistence.repository.TransferRepository;
import dev.springmind.wallet.transfers.dto.CreatePixRequest;
import dev.springmind.wallet.transfers.dto.QrPayloadResponse;
import dev.springmind.wallet.transfers.dto.TransferDto;
import dev.springmind.wallet.wallet.WalletService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PixService {

    private static final String DEMO_USER_ID = "u1";

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final NotificationRepository notificationRepository;
    private final OnboardingService onboardingService;

    public PixService(
            AccountRepository accountRepository,
            BeneficiaryRepository beneficiaryRepository,
            TransferRepository transferRepository,
            TransactionRepository transactionRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            NotificationRepository notificationRepository,
            OnboardingService onboardingService) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.notificationRepository = notificationRepository;
        this.onboardingService = onboardingService;
    }

    @Transactional
    public TransferDto executePix(CreatePixRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var cachedKey = idempotencyKeyRepository.findById(idempotencyKey);
            if (cachedKey.isPresent()) {
                TransferEntity cachedTransfer = transferRepository
                        .findById(cachedKey.get().getTransferId())
                        .orElseThrow(() -> new IllegalStateException("Transferência de idempotência não encontrada."));
                return toDto(cachedTransfer);
            }
        }

        boolean hasBeneficiary = request.beneficiaryId() != null && !request.beneficiaryId().isBlank();
        boolean hasPixKeyPair = request.pixKey() != null
                && !request.pixKey().isBlank()
                && request.pixKeyType() != null;

        if (hasBeneficiary == hasPixKeyPair) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Informe beneficiaryId ou o par pixKey + pixKeyType, nunca ambos.");
        }

        if (request.amountCents() <= 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "O valor da transferência deve ser um inteiro positivo.");
        }

        String beneficiaryId = null;
        String pixKey;
        PixKeyType pixKeyType;
        String counterparty;

        if (hasBeneficiary) {
            BeneficiaryEntity beneficiary = beneficiaryRepository
                    .findById(request.beneficiaryId())
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.BAD_REQUEST, "BENEFICIARY_NOT_FOUND", "Favorecido não encontrado."));
            beneficiaryId = beneficiary.getId();
            pixKey = beneficiary.getPixKey();
            pixKeyType = beneficiary.getPixKeyType();
            counterparty = beneficiary.getName();
        } else {
            PixKeyValidator.assertValid(request.pixKeyType(), request.pixKey());
            pixKey = request.pixKey().trim();
            pixKeyType = request.pixKeyType();
            counterparty = pixKey;
        }

        Instant now = Instant.now();
        Instant scheduledFor = request.scheduledFor();
        boolean isScheduled = scheduledFor != null;

        AccountEntity account = accountRepository
                .findByUserIdForUpdate(DEMO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Conta demo não encontrada."));
        WalletService.resetDailySpentIfNeeded(account);

        if (!isScheduled) {
            if (account.getAvailableCents() < request.amountCents()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "INSUFFICIENT_FUNDS",
                        "Saldo insuficiente para completar essa transferência.");
            }
            if (account.getDailySpentCents() + request.amountCents() > account.getDailyLimitCents()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "DAILY_LIMIT_EXCEEDED",
                        "Limite diário de PIX excedido para essa transferência.");
            }
        }

        String correlationId = CorrelationIds.current();
        String endToEndId = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        TransferEntity transfer = new TransferEntity(
                UUID.randomUUID().toString(),
                beneficiaryId,
                pixKey,
                pixKeyType,
                request.amountCents(),
                isScheduled ? TransferStatus.SCHEDULED : TransferStatus.COMPLETED,
                now,
                scheduledFor,
                endToEndId,
                correlationId);
        transferRepository.save(transfer);

        if (!isScheduled) {
            completeImmediate(account, transfer, counterparty, now);
        } else {
            accountRepository.save(account);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyKeyRepository.save(new IdempotencyKeyEntity(idempotencyKey, transfer.getId(), now));
        }

        return toDto(transfer);
    }

    @Transactional
    public void processDueScheduledTransfers() {
        Instant now = Instant.now();
        List<TransferEntity> due =
                transferRepository.findDueScheduled(TransferStatus.SCHEDULED, now);
        if (due.isEmpty()) {
            return;
        }

        AccountEntity account = accountRepository
                .findByUserIdForUpdate(DEMO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Conta demo não encontrada."));
        WalletService.resetDailySpentIfNeeded(account);

        for (TransferEntity transfer : due) {
            if (account.getAvailableCents() < transfer.getAmountCents()) {
                transfer.setStatus(TransferStatus.FAILED);
                transferRepository.save(transfer);
                notificationRepository.save(new NotificationEntity(
                        UUID.randomUUID().toString(),
                        DEMO_USER_ID,
                        "Transferência agendada falhou",
                        "Saldo insuficiente para concluir a transferência agendada.",
                        false,
                        now));
                continue;
            }
            if (account.getDailySpentCents() + transfer.getAmountCents() > account.getDailyLimitCents()) {
                transfer.setStatus(TransferStatus.FAILED);
                transferRepository.save(transfer);
                notificationRepository.save(new NotificationEntity(
                        UUID.randomUUID().toString(),
                        DEMO_USER_ID,
                        "Transferência agendada falhou",
                        "Limite diário insuficiente para concluir a transferência agendada.",
                        false,
                        now));
                continue;
            }
            String counterparty = resolveCounterparty(transfer);
            transfer.setStatus(TransferStatus.COMPLETED);
            transferRepository.save(transfer);
            applyDebit(account, transfer.getAmountCents());
            persistCompletionArtifacts(transfer, counterparty, now);
        }
        accountRepository.save(account);
    }

    public TransferDto getById(String id) {
        TransferEntity transfer = transferRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "TRANSFER_NOT_FOUND", "Transferência não encontrada."));
        return toDto(transfer);
    }

    public QrPayloadResponse buildQrPayload(long amountCents, String pixKey) {
        if (amountCents <= 0 || pixKey == null || pixKey.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "amountCents e pixKey são obrigatórios e amountCents deve ser positivo.");
        }
        return new QrPayloadResponse("MINDPIX|v1|" + pixKey.trim() + "|" + amountCents);
    }

    private void completeImmediate(
            AccountEntity account, TransferEntity transfer, String counterparty, Instant now) {
        applyDebit(account, transfer.getAmountCents());
        accountRepository.save(account);
        persistCompletionArtifacts(transfer, counterparty, now);
    }

    private void applyDebit(AccountEntity account, long amountCents) {
        account.setAvailableCents(account.getAvailableCents() - amountCents);
        account.setDailySpentCents(account.getDailySpentCents() + amountCents);
    }

    private void persistCompletionArtifacts(TransferEntity transfer, String counterparty, Instant now) {
        transactionRepository.save(new TransactionEntity(
                UUID.randomUUID().toString(),
                TransactionType.PIX_OUT,
                transfer.getAmountCents(),
                "PIX para " + counterparty,
                now,
                counterparty));
        onboardingService.markDone(OnboardingStepCode.FIRST_PIX);
        notificationRepository.save(new NotificationEntity(
                UUID.randomUUID().toString(),
                DEMO_USER_ID,
                "PIX enviado",
                "Transferência de " + transfer.getAmountCents() + " centavos para " + counterparty
                        + " foi concluída.",
                false,
                now));
    }

    private String resolveCounterparty(TransferEntity transfer) {
        if (transfer.getBeneficiaryId() != null) {
            return beneficiaryRepository
                    .findById(transfer.getBeneficiaryId())
                    .map(BeneficiaryEntity::getName)
                    .orElse(transfer.getPixKey() != null ? transfer.getPixKey() : "Destinatário");
        }
        return transfer.getPixKey() != null ? transfer.getPixKey() : "Destinatário";
    }

    private TransferDto toDto(TransferEntity transfer) {
        return new TransferDto(
                transfer.getId(),
                transfer.getBeneficiaryId(),
                transfer.getPixKey(),
                transfer.getPixKeyType(),
                transfer.getAmountCents(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                transfer.getScheduledFor(),
                transfer.getEndToEndId(),
                transfer.getCorrelationId());
    }
}
