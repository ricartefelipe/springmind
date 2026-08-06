package dev.springmind.wallet.onboarding;

import dev.springmind.wallet.onboarding.dto.OnboardingStateResponse;
import dev.springmind.wallet.onboarding.dto.OnboardingStepDto;
import dev.springmind.wallet.persistence.entity.OnboardingStepCode;
import dev.springmind.wallet.persistence.entity.OnboardingStepEntity;
import dev.springmind.wallet.persistence.entity.OnboardingStepPk;
import dev.springmind.wallet.persistence.repository.OnboardingStepRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private static final String DEMO_USER_ID = "u1";

    private final OnboardingStepRepository onboardingStepRepository;

    public OnboardingService(OnboardingStepRepository onboardingStepRepository) {
        this.onboardingStepRepository = onboardingStepRepository;
    }

    public OnboardingStateResponse getState() {
        return getState(DEMO_USER_ID);
    }

    public OnboardingStateResponse getState(String userId) {
        Map<OnboardingStepCode, Boolean> doneByStep = new EnumMap<>(OnboardingStepCode.class);
        for (OnboardingStepEntity entity : onboardingStepRepository.findByUserId(userId)) {
            doneByStep.put(entity.getStep(), entity.isDone());
        }
        List<OnboardingStepDto> steps = Arrays.stream(OnboardingStepCode.values())
                .map(code -> new OnboardingStepDto(code.name(), doneByStep.getOrDefault(code, false)))
                .toList();
        boolean completed = steps.stream().allMatch(OnboardingStepDto::done);
        return new OnboardingStateResponse(steps, completed);
    }

    @Transactional
    public void markDone(OnboardingStepCode step) {
        markDone(DEMO_USER_ID, step);
    }

    @Transactional
    public void markDone(String userId, OnboardingStepCode step) {
        OnboardingStepPk pk = new OnboardingStepPk(userId, step);
        OnboardingStepEntity entity = onboardingStepRepository
                .findById(pk)
                .orElseGet(() -> new OnboardingStepEntity(userId, step, false));
        entity.setDone(true);
        onboardingStepRepository.save(entity);
    }
}
