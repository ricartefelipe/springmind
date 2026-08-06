DELETE FROM idempotency_keys;
DELETE FROM transfers;
DELETE FROM transactions WHERE id <> 't1';
UPDATE accounts
SET available_cents = 250000,
    blocked_cents = 10000,
    daily_limit_cents = 100000,
    daily_spent_cents = 0,
    daily_spent_on = NULL
WHERE id = 'a1';
