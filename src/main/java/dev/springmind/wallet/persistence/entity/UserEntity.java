package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column
    private String role;

    protected UserEntity() {}

    public UserEntity(
            String id,
            String name,
            String email,
            String passwordHash,
            boolean enabled,
            Instant expiresAt,
            String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.expiresAt = expiresAt;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getRole() {
        return role;
    }

    public void update(String name, String passwordHash, Instant expiresAt, String role) {
        this.name = name;
        this.passwordHash = passwordHash;
        this.enabled = true;
        this.expiresAt = expiresAt;
        this.role = role;
    }

    public void disable() {
        this.enabled = false;
    }

    public void revoke() {
        this.enabled = false;
        this.expiresAt = Instant.now();
    }
}
