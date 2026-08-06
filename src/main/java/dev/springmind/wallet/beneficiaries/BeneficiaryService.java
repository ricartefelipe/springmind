package dev.springmind.wallet.beneficiaries;

import dev.springmind.wallet.beneficiaries.dto.BeneficiaryDto;
import dev.springmind.wallet.beneficiaries.dto.CreateBeneficiaryRequest;
import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.common.PixKeyValidator;
import dev.springmind.wallet.onboarding.OnboardingService;
import dev.springmind.wallet.persistence.entity.BeneficiaryEntity;
import dev.springmind.wallet.persistence.entity.OnboardingStepCode;
import dev.springmind.wallet.persistence.repository.BeneficiaryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final OnboardingService onboardingService;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository, OnboardingService onboardingService) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.onboardingService = onboardingService;
    }

    public List<BeneficiaryDto> list() {
        return beneficiaryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BeneficiaryDto create(CreateBeneficiaryRequest request) {
        if (isBlank(request.name()) || isBlank(request.pixKey()) || request.pixKeyType() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Nome, chave PIX e tipo da chave são obrigatórios.");
        }

        PixKeyValidator.assertValid(request.pixKeyType(), request.pixKey());

        BeneficiaryEntity beneficiary = new BeneficiaryEntity(
                UUID.randomUUID().toString(), request.name().trim(), request.pixKey().trim(), request.pixKeyType());
        beneficiaryRepository.save(beneficiary);
        onboardingService.markDone(OnboardingStepCode.FIRST_BENEFICIARY);
        return toDto(beneficiary);
    }

    public void delete(String id) {
        if (!beneficiaryRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND", "Favorecido não encontrado.");
        }
        beneficiaryRepository.deleteById(id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BeneficiaryDto toDto(BeneficiaryEntity beneficiary) {
        return new BeneficiaryDto(
                beneficiary.getId(),
                beneficiary.getName(),
                beneficiary.getPixKey(),
                beneficiary.getPixKeyType());
    }
}
