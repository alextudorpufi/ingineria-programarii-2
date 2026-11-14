-- V6 marked every pre-existing professional as verified, which included public
-- self-registrations created before the verification gate existed. Revoke that
-- blanket trust so the administrator approval step is meaningful. Existing
-- professionals can be re-verified through POST /api/admin/verify/{userId}.
UPDATE users
SET pro_verified = FALSE,
    version = version + 1,
    updated_at = CURRENT_TIMESTAMP
WHERE role = 'pro'
  AND pro_verified = TRUE;
