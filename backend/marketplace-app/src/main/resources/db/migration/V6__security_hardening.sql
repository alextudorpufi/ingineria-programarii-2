-- ==============================================================================
-- V6: Security hardening
-- Adds professional verification, login-lockout state, and an auditable
-- record of administrator-granted free credits.
-- ==============================================================================

-- Public professional registrations start unverified and must be approved by
-- an administrator before they can purchase credits.
ALTER TABLE users ADD COLUMN IF NOT EXISTS pro_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Consecutive failed-login counter and temporary lockout window.
ALTER TABLE users ADD COLUMN IF NOT EXISTS login_attempts_count INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS lockout_until TIMESTAMP WITHOUT TIME ZONE;

-- Accounts that existed before this migration are trusted, so keep them usable.
UPDATE users SET pro_verified = TRUE WHERE role IN ('pro', 'admin');

-- One row per administrator-approved credit grant. The unique "one grant per
-- user" invariant is enforced by users.pro_verified plus the admin workflow.
CREATE TABLE IF NOT EXISTS credit_grants (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL,
    admin_id UUID REFERENCES users(id) ON DELETE SET NULL,
    granted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT chk_credit_grants_amount_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_credit_grants_user_id ON credit_grants(user_id);
