package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "onboarding_steps")
@IdClass(OnboardingStepPk.class)
public class OnboardingStepEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStepCode step;

    @Column(nullable = false)
    private boolean done;

    protected OnboardingStepEntity() {}

    public OnboardingStepEntity(String userId, OnboardingStepCode step, boolean done) {
        this.userId = userId;
        this.step = step;
        this.done = done;
    }

    public String getUserId() {
        return userId;
    }

    public OnboardingStepCode getStep() {
        return step;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
