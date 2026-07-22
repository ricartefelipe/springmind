CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    available_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL
);

CREATE TABLE beneficiaries (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pix_key VARCHAR(255) NOT NULL
);

CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount_cents BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    counterparty VARCHAR(255) NOT NULL
);

CREATE TABLE transfers (
    id VARCHAR(36) PRIMARY KEY,
    beneficiary_id VARCHAR(36) NOT NULL REFERENCES beneficiaries(id),
    amount_cents BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE idempotency_keys (
    key_value VARCHAR(255) PRIMARY KEY,
    transfer_id VARCHAR(36) NOT NULL REFERENCES transfers(id),
    created_at TIMESTAMPTZ NOT NULL
);
