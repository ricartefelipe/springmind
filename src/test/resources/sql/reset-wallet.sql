DELETE FROM idempotency_keys;
DELETE FROM transfers;
DELETE FROM transactions WHERE id <> 't1';
UPDATE accounts SET available_cents = 250000 WHERE id = 'a1';
