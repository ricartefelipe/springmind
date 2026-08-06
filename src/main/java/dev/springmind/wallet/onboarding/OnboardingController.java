package dev.springmind.wallet.onboarding;

import dev.springmind.wallet.onboarding.dto.OnboardingStateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/onboarding")
    public OnboardingStateResponse getOnboarding() {
        return onboardingService.getState();
    }
}
