package dev.springmind.wallet.beneficiaries;

import dev.springmind.wallet.beneficiaries.dto.BeneficiaryDto;
import dev.springmind.wallet.beneficiaries.dto.CreateBeneficiaryRequest;
import dev.springmind.wallet.common.ApiException;
import dev.springmind.wallet.persistence.entity.BeneficiaryEntity;
import dev.springmind.wallet.persistence.repository.BeneficiaryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public List<BeneficiaryDto> list() {
        return beneficiaryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public BeneficiaryDto create(CreateBeneficiaryRequest request) {
        if (isBlank(request.name()) || isBlank(request.pixKey())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_BENEFICIARY", "Nome e chave PIX são obrigatórios.");
        }

        BeneficiaryEntity beneficiary =
                new BeneficiaryEntity(UUID.randomUUID().toString(), request.name(), request.pixKey());
        beneficiaryRepository.save(beneficiary);
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
        return new BeneficiaryDto(beneficiary.getId(), beneficiary.getName(), beneficiary.getPixKey());
    }
}
