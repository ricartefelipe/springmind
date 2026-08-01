package dev.springmind.wallet.totalrecall;

import java.time.Instant;

public record TotalRecallUserRequest(
        String email,
        String name,
        String password,
        String role,
        Instant expiresAt,
        ProvisionAction action) {}
