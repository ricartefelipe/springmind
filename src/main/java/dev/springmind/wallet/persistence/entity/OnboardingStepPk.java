package dev.springmind.wallet.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class OnboardingStepPk implements Serializable {

    private String userId;
    private OnboardingStepCode step;

    public OnboardingStepPk() {}

    public OnboardingStepPk(String userId, OnboardingStepCode step) {
        this.userId = userId;
        this.step = step;
    }

    public String getUserId() {
        return userId;
    }

    public OnboardingStepCode getStep() {
        return step;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OnboardingStepPk that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && step == that.step;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, step);
    }
}
