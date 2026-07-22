package dev.springmind.wallet.persistence.repository;

import dev.springmind.wallet.persistence.entity.BeneficiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, String> {}
