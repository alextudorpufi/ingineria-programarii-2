-- Neutral trust defaults: a row inserted without explicit trust signals must not
-- look verified or highly rated. Application code writes real values on the
-- normal paths; these defaults only protect raw inserts and future code paths.
ALTER TABLE users ALTER COLUMN rating SET DEFAULT 0.00;
ALTER TABLE users ALTER COLUMN trust_score SET DEFAULT 0.00;

ALTER TABLE leads ALTER COLUMN client_is_verified SET DEFAULT FALSE;
ALTER TABLE leads ALTER COLUMN passport_completeness_score SET DEFAULT 0;
ALTER TABLE leads ALTER COLUMN passport_coherence_status SET DEFAULT 'Nedeterminat';
ALTER TABLE leads ALTER COLUMN passport_contact_verified SET DEFAULT FALSE;
ALTER TABLE leads ALTER COLUMN passport_intent_verified SET DEFAULT FALSE;
ALTER TABLE leads ALTER COLUMN passport_extracted_by_ai SET DEFAULT FALSE;
ALTER TABLE leads ALTER COLUMN passport_duplicate_risk SET DEFAULT 'Nedeterminat';

-- Enum columns previously accepted any string, so a bad write made every later
-- read fail. Constrain the stored vocabulary to the values the domain enums
-- produce.
ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('client', 'pro', 'admin'));
ALTER TABLE leads
    ADD CONSTRAINT chk_leads_status
        CHECK (status IN ('validat', 'in_asteptare', 'atribuit', 'finalizat', 'disputat'));
ALTER TABLE disputes
    ADD CONSTRAINT chk_disputes_status CHECK (status IN ('in_analiza', 'aprobat', 'respins'));
ALTER TABLE disputes
    ADD CONSTRAINT chk_disputes_reason
        CHECK (reason IN ('wrong_category', 'unreachable_contact', 'incompatible_budget', 'duplicate_or_fake'));
ALTER TABLE wallet_transactions
    ADD CONSTRAINT chk_wallet_transactions_type
        CHECK (type IN ('lead_purchase', 'refund', 'top_up', 'bonus'));
ALTER TABLE audit_logs
    ADD CONSTRAINT chk_audit_logs_action CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'CLAIM_LEAD',
        'CREDIT_DEDUCTION', 'CREDIT_TOP_UP', 'CREDIT_BONUS', 'REFUND_APPROVED',
        'RAISE_DISPUTE', 'RESOLVE_DISPUTE', 'VERIFY_PROFESSIONAL'
    ));
