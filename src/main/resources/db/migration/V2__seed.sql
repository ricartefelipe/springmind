INSERT INTO users (id, name, email, password_hash)
VALUES ('u1', 'Marion Demo', 'demo@vuemind.dev', 'demo123');

INSERT INTO accounts (id, user_id, available_cents, currency)
VALUES ('a1', 'u1', 250000, 'BRL');

INSERT INTO beneficiaries (id, name, pix_key) VALUES
    ('b1', 'Ana Silva', 'ana@email.com'),
    ('b2', 'Mercado Central', '11222333000181');

INSERT INTO transactions (id, type, amount_cents, description, created_at, counterparty)
VALUES ('t1', 'PIX_IN', 50000, 'Recebido', NOW() - INTERVAL '2 days', 'Carlos');
