package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "available_cents", nullable = false)
    private long availableCents;

    @Column(name = "blocked_cents", nullable = false)
    private long blockedCents;

    @Column(name = "daily_limit_cents", nullable = false)
    private long dailyLimitCents;

    @Column(name = "daily_spent_cents", nullable = false)
    private long dailySpentCents;

    @Column(name = "daily_spent_on")
    private LocalDate dailySpentOn;

    @Column(nullable = false)
    private String currency;

    protected AccountEntity() {}

    public AccountEntity(String id, String userId, long availableCents, String currency) {
        this(id, userId, availableCents, 0L, 0L, 0L, null, currency);
    }

    public AccountEntity(
            String id,
            String userId,
            long availableCents,
            long blockedCents,
            long dailyLimitCents,
            long dailySpentCents,
            LocalDate dailySpentOn,
            String currency) {
        this.id = id;
        this.userId = userId;
        this.availableCents = availableCents;
        this.blockedCents = blockedCents;
        this.dailyLimitCents = dailyLimitCents;
        this.dailySpentCents = dailySpentCents;
        this.dailySpentOn = dailySpentOn;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getAvailableCents() {
        return availableCents;
    }

    public void setAvailableCents(long availableCents) {
        this.availableCents = availableCents;
    }

    public long getBlockedCents() {
        return blockedCents;
    }

    public void setBlockedCents(long blockedCents) {
        this.blockedCents = blockedCents;
    }

    public long getDailyLimitCents() {
        return dailyLimitCents;
    }

    public void setDailyLimitCents(long dailyLimitCents) {
        this.dailyLimitCents = dailyLimitCents;
    }

    public long getDailySpentCents() {
        return dailySpentCents;
    }

    public void setDailySpentCents(long dailySpentCents) {
        this.dailySpentCents = dailySpentCents;
    }

    public LocalDate getDailySpentOn() {
        return dailySpentOn;
    }

    public void setDailySpentOn(LocalDate dailySpentOn) {
        this.dailySpentOn = dailySpentOn;
    }

    public String getCurrency() {
        return currency;
    }
}
