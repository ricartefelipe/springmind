package dev.springmind.wallet.persistence.repository;

import dev.springmind.wallet.persistence.entity.OnboardingStepEntity;
import dev.springmind.wallet.persistence.entity.OnboardingStepPk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingStepRepository extends JpaRepository<OnboardingStepEntity, OnboardingStepPk> {

    List<OnboardingStepEntity> findByUserId(String userId);
}
