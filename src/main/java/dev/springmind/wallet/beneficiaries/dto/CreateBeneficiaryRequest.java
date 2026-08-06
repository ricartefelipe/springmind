package dev.springmind.wallet.beneficiaries.dto;

import dev.springmind.wallet.persistence.entity.PixKeyType;

public record CreateBeneficiaryRequest(String name, String pixKey, PixKeyType pixKeyType) {}
