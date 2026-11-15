-- ==============================================================================
-- V8: Enforce the one-lifetime-grant invariant at the database level.
-- ==============================================================================

-- Keep the earliest grant per user if historical duplicates exist.
DELETE FROM credit_grants
WHERE id IN (
    SELECT id
    FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY granted_at, id) AS rn
        FROM credit_grants
    ) ranked
    WHERE rn > 1
);

DROP INDEX IF EXISTS idx_credit_grants_user_id;
CREATE UNIQUE INDEX IF NOT EXISTS uq_credit_grants_user_id ON credit_grants(user_id);
