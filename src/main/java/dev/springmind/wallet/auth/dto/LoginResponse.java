package dev.springmind.wallet.auth.dto;

public record LoginResponse(String accessToken, UserDto user) {}
