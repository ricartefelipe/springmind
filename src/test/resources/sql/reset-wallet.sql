DELETE FROM idempotency_keys;
DELETE FROM transfers;
DELETE FROM transactions WHERE id <> 't1';
DELETE FROM notifications WHERE id NOT IN ('n1', 'n2', 'n3');
UPDATE notifications SET "read" = FALSE WHERE id IN ('n1', 'n2', 'n3');
DELETE FROM beneficiaries WHERE id NOT IN ('b1', 'b2');
UPDATE onboarding_steps
SET done = CASE step
    WHEN 'PROFILE_OK' THEN TRUE
    WHEN 'FIRST_BENEFICIARY' THEN TRUE
    ELSE FALSE
END
WHERE user_id = 'u1';
UPDATE accounts
SET available_cents = 250000,
    blocked_cents = 10000,
    daily_limit_cents = 100000,
    daily_spent_cents = 0,
    daily_spent_on = NULL
WHERE id = 'a1';
