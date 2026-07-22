package dev.springmind.wallet.transfers;

import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.persistence.entity.AccountEntity;
import dev.springmind.wallet.persistence.entity.BeneficiaryEntity;
import dev.springmind.wallet.persistence.entity.IdempotencyKeyEntity;
import dev.springmind.wallet.persistence.entity.TransactionEntity;
import dev.springmind.wallet.persistence.entity.TransactionType;
import dev.springmind.wallet.persistence.entity.TransferEntity;
import dev.springmind.wallet.persistence.repository.AccountRepository;
import dev.springmind.wallet.persistence.repository.BeneficiaryRepository;
import dev.springmind.wallet.persistence.repository.IdempotencyKeyRepository;
import dev.springmind.wallet.persistence.repository.TransactionRepository;
import dev.springmind.wallet.persistence.repository.TransferRepository;
import dev.springmind.wallet.transfers.dto.CreatePixRequest;
import dev.springmind.wallet.transfers.dto.TransferDto;
import java.time.Instant;
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

    public PixService(
            AccountRepository accountRepository,
            BeneficiaryRepository beneficiaryRepository,
            TransferRepository transferRepository,
            TransactionRepository transactionRepository,
            IdempotencyKeyRepository idempotencyKeyRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transferRepository = transferRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public TransferDto executePix(CreatePixRequest request, String idempotencyKey) {
        var cachedKey = idempotencyKeyRepository.findById(idempotencyKey);
        if (cachedKey.isPresent()) {
            TransferEntity cachedTransfer = transferRepository.findById(cachedKey.get().getTransferId())
                    .orElseThrow(() -> new IllegalStateException("Transferência de idempotência não encontrada."));
            return toDto(cachedTransfer);
        }

        BeneficiaryEntity beneficiary = beneficiaryRepository.findById(request.beneficiaryId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST, "BENEFICIARY_NOT_FOUND", "Favorecido não encontrado."));

        if (request.amountCents() <= 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "O valor da transferência deve ser positivo.");
        }

        AccountEntity account = accountRepository.findByUserIdForUpdate(DEMO_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Conta demo não encontrada."));

        if (account.getAvailableCents() < request.amountCents()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_FUNDS",
                    "Saldo insuficiente para completar essa transferência.");
        }

        account.setAvailableCents(account.getAvailableCents() - request.amountCents());
        accountRepository.save(account);

        Instant now = Instant.now();
        TransferEntity transfer = new TransferEntity(
                UUID.randomUUID().toString(),
                request.beneficiaryId(),
                request.amountCents(),
                "COMPLETED",
                now);
        transferRepository.save(transfer);

        TransactionEntity transaction = new TransactionEntity(
                UUID.randomUUID().toString(),
                TransactionType.PIX_OUT,
                request.amountCents(),
                "PIX para " + beneficiary.getName(),
                now,
                beneficiary.getName());
        transactionRepository.save(transaction);

        idempotencyKeyRepository.save(new IdempotencyKeyEntity(idempotencyKey, transfer.getId(), now));

        return toDto(transfer);
    }

    public TransferDto getById(String id) {
        TransferEntity transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "TRANSFER_NOT_FOUND", "Transferência não encontrada."));
        return toDto(transfer);
    }

    private TransferDto toDto(TransferEntity transfer) {
        return new TransferDto(
                transfer.getId(),
                transfer.getBeneficiaryId(),
                transfer.getAmountCents(),
                transfer.getStatus(),
                transfer.getCreatedAt());
    }
}
