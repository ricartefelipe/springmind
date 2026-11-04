package dev.springmind.wallet.persistence.entity;

/** Tipos de chave PIX do contrato da carteira (os mesmos do front e da vuemind-api). */
public enum PixKeyType {
    EMAIL,
    CPF,
    CNPJ,
    PHONE,
    RANDOM
}
