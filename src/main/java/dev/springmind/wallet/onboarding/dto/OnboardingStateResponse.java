package dev.springmind.wallet.onboarding.dto;

import java.util.List;

public record OnboardingStateResponse(List<OnboardingStepDto> steps, boolean completed) {}
