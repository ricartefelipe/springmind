package dev.springmind.wallet.beneficiaries.dto;

import dev.springmind.wallet.persistence.entity.PixKeyType;

public record BeneficiaryDto(String id, String name, String pixKey, PixKeyType pixKeyType) {}
