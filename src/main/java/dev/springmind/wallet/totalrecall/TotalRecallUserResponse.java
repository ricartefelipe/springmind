package dev.springmind.wallet.totalrecall;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TotalRecallUserResponse(
        String id,
        String email,
        String name,
        boolean enabled,
        String role,
        Instant expiresAt) {}
