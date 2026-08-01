ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN expires_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN role VARCHAR(100);

UPDATE users
SET password_hash = '$2b$12$yZP2TSszU.mt/ObGJ.FOwO7UgCY9kZE9SFpv12uhTz.jbiuZY7puW'
WHERE email = 'demo@vuemind.dev';
