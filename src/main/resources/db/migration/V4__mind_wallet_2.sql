ALTER TABLE accounts ADD COLUMN blocked_cents BIGINT NOT NULL DEFAULT 0;
ALTER TABLE accounts ADD COLUMN daily_limit_cents BIGINT NOT NULL DEFAULT 0;
ALTER TABLE accounts ADD COLUMN daily_spent_cents BIGINT NOT NULL DEFAULT 0;
ALTER TABLE accounts ADD COLUMN daily_spent_on DATE;

UPDATE accounts
SET blocked_cents = 10000,
    daily_limit_cents = 100000,
    daily_spent_cents = 0,
    daily_spent_on = NULL
WHERE id = 'a1';

ALTER TABLE beneficiaries ADD COLUMN pix_key_type VARCHAR(20) NOT NULL DEFAULT 'EMAIL';

UPDATE beneficiaries SET pix_key_type = 'EMAIL' WHERE id = 'b1';
UPDATE beneficiaries SET pix_key_type = 'RANDOM' WHERE id = 'b2';

ALTER TABLE transfers ALTER COLUMN beneficiary_id DROP NOT NULL;
ALTER TABLE transfers ADD COLUMN pix_key VARCHAR(255);
ALTER TABLE transfers ADD COLUMN pix_key_type VARCHAR(20);
ALTER TABLE transfers ADD COLUMN scheduled_for TIMESTAMPTZ;
ALTER TABLE transfers ADD COLUMN end_to_end_id VARCHAR(255);
ALTER TABLE transfers ADD COLUMN correlation_id VARCHAR(36);

CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    "read" BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE onboarding_steps (
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    step VARCHAR(50) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, step)
);

INSERT INTO onboarding_steps (user_id, step, done) VALUES
    ('u1', 'PROFILE_OK', TRUE),
    ('u1', 'FIRST_BENEFICIARY', TRUE),
    ('u1', 'FIRST_PIX', FALSE),
    ('u1', 'VIEW_STATEMENT', FALSE);

INSERT INTO notifications (id, user_id, title, body, "read", created_at) VALUES
    ('n1', 'u1', 'PIX recebido', 'Você recebeu um PIX de R$ 500,00 de Carlos.', FALSE, '2026-07-20T10:00:05Z'),
    ('n2', 'u1', 'Limite diário próximo', 'Você já utilizou uma parte relevante do seu limite diário de PIX.', FALSE, '2026-07-24T18:00:00Z'),
    ('n3', 'u1', 'Complete seu onboarding', 'Cadastre um favorecido e faça seu primeiro PIX para liberar todo o app.', FALSE, '2026-07-25T09:00:00Z');
