-- ==============================================================================
-- V0: Marketplace baseline schema and seed data.
--
-- This is the single pre-release baseline. It replaces the previous V1..V11
-- chain (initial schema, Stripe purchases, professional-profile consolidation,
-- lead matching, security hardening, trust defaults, customer optimistic
-- locking) with the schema those migrations produced on a fresh database.
--
-- Historical data migrations are intentionally absent: there are no legacy
-- databases to upgrade, so the legacy profile tables and the old professional
-- columns never exist here.
-- ==============================================================================

-- 1. Customers
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- 2. Service Categories & Subcategories
CREATE TABLE service_categories (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_name VARCHAR(100),
    description TEXT,
    avg_lead_price INT NOT NULL DEFAULT 30,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100)
);

CREATE TABLE service_subcategories (
    id UUID PRIMARY KEY,
    category_id VARCHAR(100) NOT NULL REFERENCES service_categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subcategories_category_id ON service_subcategories(category_id);

-- 3. Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'client', -- 'client', 'pro', 'admin'
    name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    title VARCHAR(255),
    phone VARCHAR(50),
    avatar_url VARCHAR(1024),
    bio TEXT,
    verified_badge BOOLEAN NOT NULL DEFAULT FALSE,
    rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    review_count INT NOT NULL DEFAULT 0,
    credits_balance INT NOT NULL DEFAULT 0,
    hourly_rate NUMERIC(10, 2),
    client_budget NUMERIC(12, 2),
    response_time VARCHAR(100) DEFAULT '< 15 minute',
    completed_jobs INT NOT NULL DEFAULT 0,
    trust_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    member_since VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100),
    pro_verified BOOLEAN NOT NULL DEFAULT FALSE,
    login_attempts_count INT NOT NULL DEFAULT 0,
    lockout_until TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT chk_users_credits_non_negative CHECK (credits_balance >= 0),
    CONSTRAINT chk_users_role CHECK (role IN ('client', 'pro', 'admin'))
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email_lower ON users (LOWER(email));

-- 4. Professional Profiles
-- One profile per account. JSON arrays retain multiple values without
-- comma-separated strings.
CREATE TABLE user_professional (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    categories JSONB NOT NULL DEFAULT '[]',
    services_offered JSONB NOT NULL DEFAULT '[]',
    coverage_areas JSONB NOT NULL DEFAULT '[]',
    capabilities JSONB NOT NULL DEFAULT '[]',
    supports_onsite BOOLEAN,
    supports_remote BOOLEAN,
    years_experience INTEGER CHECK (years_experience >= 0),
    accepting_leads BOOLEAN NOT NULL DEFAULT FALSE,
    onboarding_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (onboarding_status IN ('DRAFT', 'COMPLETE')),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100),
    CONSTRAINT chk_professional_arrays CHECK (
        jsonb_typeof(categories) = 'array'
        AND jsonb_typeof(services_offered) = 'array'
        AND jsonb_typeof(coverage_areas) = 'array'
        AND jsonb_typeof(capabilities) = 'array'
    ),
    CONSTRAINT chk_professional_accepting CHECK (NOT accepting_leads OR onboarding_status = 'COMPLETE')
);

CREATE INDEX idx_professional_categories ON user_professional USING GIN (categories);
CREATE INDEX idx_professional_services ON user_professional USING GIN (services_offered);
CREATE INDEX idx_professional_coverage ON user_professional USING GIN (coverage_areas);

-- 5. Leads (Service Requests)
CREATE TABLE leads (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category_id VARCHAR(100) NOT NULL REFERENCES service_categories(id) ON DELETE RESTRICT,
    subcategory VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,

    -- Budget
    budget_min NUMERIC(12, 2) NOT NULL,
    budget_max NUMERIC(12, 2) NOT NULL,
    budget_currency VARCHAR(20) NOT NULL DEFAULT 'RON',
    budget_is_negotiable BOOLEAN NOT NULL DEFAULT TRUE,
    budget_is_confirmed BOOLEAN NOT NULL DEFAULT TRUE,

    timeline VARCHAR(255) NOT NULL,
    lead_price INT NOT NULL DEFAULT 30,
    max_pros INT NOT NULL DEFAULT 4,
    current_pros INT NOT NULL DEFAULT 0,

    -- Client Contact
    client_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    client_name VARCHAR(255) NOT NULL,
    client_phone VARCHAR(50) NOT NULL,
    client_email VARCHAR(255) NOT NULL,
    client_city VARCHAR(255) NOT NULL,
    client_is_verified BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(50) NOT NULL DEFAULT 'validat', -- 'validat', 'in_asteptare', 'atribuit', 'finalizat', 'disputat'

    -- Lead Passport (AI & Verification Quality)
    passport_completeness_score INT NOT NULL DEFAULT 0,
    passport_coherence_status VARCHAR(50) NOT NULL DEFAULT 'Nedeterminat',
    passport_contact_verified BOOLEAN NOT NULL DEFAULT FALSE,
    passport_intent_verified BOOLEAN NOT NULL DEFAULT FALSE,
    passport_required_deliverables TEXT,
    passport_clarifications_remaining TEXT,
    passport_extracted_by_ai BOOLEAN NOT NULL DEFAULT FALSE,
    passport_duplicate_risk VARCHAR(50) NOT NULL DEFAULT 'Nedeterminat',

    -- Match Explanation
    match_overall_score INT,
    match_deterministic_fit BOOLEAN,
    match_reasons TEXT,
    match_semantic_keywords TEXT,
    match_distance_km NUMERIC(6, 2),
    match_expected_response_time VARCHAR(100),

    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100),

    -- Matching Requirements
    work_mode VARCHAR(20),
    required_capabilities JSONB NOT NULL DEFAULT '[]'::jsonb,
    preferred_capabilities JSONB NOT NULL DEFAULT '[]'::jsonb,

    CONSTRAINT chk_leads_current_pros CHECK (current_pros >= 0 AND current_pros <= max_pros),
    CONSTRAINT chk_leads_budget_order CHECK (budget_min >= 0 AND budget_max >= budget_min),
    CONSTRAINT chk_leads_price_positive CHECK (lead_price > 0 AND max_pros > 0),
    CONSTRAINT chk_leads_status
        CHECK (status IN ('validat', 'in_asteptare', 'atribuit', 'finalizat', 'disputat')),
    CONSTRAINT chk_lead_work_mode CHECK (work_mode IN ('ONSITE', 'REMOTE')),
    CONSTRAINT chk_lead_required_capabilities CHECK (
        CASE WHEN jsonb_typeof(required_capabilities) = 'array'
            THEN jsonb_array_length(required_capabilities) <= 50 ELSE false END
    ),
    CONSTRAINT chk_lead_preferred_capabilities CHECK (
        CASE WHEN jsonb_typeof(preferred_capabilities) = 'array'
            THEN jsonb_array_length(preferred_capabilities) <= 50 ELSE false END
    )
);

CREATE INDEX idx_leads_category ON leads(category_id);
CREATE INDEX idx_leads_client ON leads(client_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_created_at ON leads(created_at);

-- 6. Claimed Pros on Leads
CREATE TABLE lead_claims (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    pro_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    pro_name VARCHAR(255) NOT NULL,
    claimed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_lead_claims_lead_pro UNIQUE (lead_id, pro_id)
);

CREATE INDEX idx_lead_claims_lead_id ON lead_claims(lead_id);
CREATE INDEX idx_lead_claims_pro_id ON lead_claims(pro_id);

-- 7. Wallet Transactions (Credits Integrity)
-- Append-only ledger. Application code must never UPDATE or DELETE.
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    type VARCHAR(50) NOT NULL, -- 'lead_purchase', 'refund', 'top_up', 'bonus'
    amount INT NOT NULL, -- positive or negative
    description VARCHAR(500) NOT NULL,
    balance_after INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    CONSTRAINT chk_wallet_balance_non_negative CHECK (balance_after >= 0),
    CONSTRAINT chk_wallet_transactions_type
        CHECK (type IN ('lead_purchase', 'refund', 'top_up', 'bonus'))
);

CREATE INDEX idx_wallet_tx_user_id ON wallet_transactions(user_id);
CREATE INDEX idx_wallet_tx_lead_id ON wallet_transactions(lead_id);
CREATE INDEX idx_wallet_tx_created_at ON wallet_transactions(created_at);

-- 8. Credit Purchases (Stripe Checkout)
CREATE TABLE credit_purchases (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    bundle_id VARCHAR(50) NOT NULL,
    credits INT NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    stripe_session_id VARCHAR(255) UNIQUE,
    checkout_url VARCHAR(2048),
    wallet_transaction_id UUID UNIQUE REFERENCES wallet_transactions(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uq_credit_purchases_user_idempotency UNIQUE (user_id, idempotency_key),
    CONSTRAINT chk_credit_purchases_credits_positive CHECK (credits > 0),
    CONSTRAINT chk_credit_purchases_amount_positive CHECK (amount_minor > 0),
    CONSTRAINT chk_credit_purchases_status CHECK (status IN ('pending', 'paid', 'failed', 'expired')),
    CONSTRAINT chk_credit_purchases_payment_consistency CHECK (
        (status = 'paid' AND wallet_transaction_id IS NOT NULL)
        OR (status <> 'paid' AND wallet_transaction_id IS NULL)
    )
);

CREATE INDEX idx_credit_purchases_user_created
    ON credit_purchases(user_id, created_at DESC);

CREATE TABLE stripe_webhook_events (
    event_id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    received_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 9. Disputes & Refund Ledger
CREATE TABLE disputes (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE RESTRICT,
    lead_title VARCHAR(255) NOT NULL,
    pro_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    pro_name VARCHAR(255) NOT NULL,
    reason VARCHAR(100) NOT NULL, -- 'wrong_category', 'unreachable_contact', 'incompatible_budget', 'duplicate_or_fake'
    details TEXT NOT NULL,
    credits_requested INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'in_analiza', -- 'in_analiza', 'aprobat', 'respins'
    admin_decision_note TEXT,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100),
    CONSTRAINT chk_disputes_credits_positive CHECK (credits_requested > 0),
    CONSTRAINT chk_disputes_status CHECK (status IN ('in_analiza', 'aprobat', 'respins')),
    CONSTRAINT chk_disputes_reason
        CHECK (reason IN ('wrong_category', 'unreachable_contact', 'incompatible_budget', 'duplicate_or_fake'))
);

CREATE INDEX idx_disputes_lead_id ON disputes(lead_id);
CREATE INDEX idx_disputes_pro_id ON disputes(pro_id);
CREATE INDEX idx_disputes_status ON disputes(status);

-- 10. Chat Messages
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    sender_role VARCHAR(50) NOT NULL, -- 'client', 'pro', 'admin', 'system'
    content TEXT NOT NULL,
    is_offer BOOLEAN NOT NULL DEFAULT FALSE,
    offer_amount NUMERIC(12, 2),
    sent_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_chat_messages_lead_id ON chat_messages(lead_id);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_sent_at ON chat_messages(sent_at);

-- 11. Ratings (Verified Rating Protocol)
CREATE TABLE ratings (
    id UUID PRIMARY KEY,
    pro_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE RESTRICT,
    lead_title VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_city VARCHAR(255),
    score_timeliness INT NOT NULL CHECK (score_timeliness BETWEEN 1 AND 5),
    score_quality INT NOT NULL CHECK (score_quality BETWEEN 1 AND 5),
    score_communication INT NOT NULL CHECK (score_communication BETWEEN 1 AND 5),
    score_budget_respect INT NOT NULL CHECK (score_budget_respect BETWEEN 1 AND 5),
    overall_score NUMERIC(3, 2) NOT NULL,
    comment TEXT,
    verified_status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ratings_lead_pro UNIQUE (lead_id, pro_id)
);

CREATE INDEX idx_ratings_pro_id ON ratings(pro_id);
CREATE INDEX idx_ratings_lead_id ON ratings(lead_id);

-- 12. Database Audit Trail Ledger
-- Append-only ledger. Application code must never UPDATE or DELETE.
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'CLAIM_LEAD', 'CREDIT_DEDUCTION', 'REFUND_APPROVED'
    changed_by VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_logs_action CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'CLAIM_LEAD',
        'CREDIT_DEDUCTION', 'CREDIT_TOP_UP', 'CREDIT_BONUS', 'REFUND_APPROVED',
        'RAISE_DISPUTE', 'RESOLVE_DISPUTE', 'VERIFY_PROFESSIONAL'
    ))
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- 13. Administrator-Granted Credits
-- One lifetime grant per user is enforced by the unique index below.
CREATE TABLE credit_grants (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL,
    admin_id UUID REFERENCES users(id) ON DELETE SET NULL,
    granted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT chk_credit_grants_amount_positive CHECK (amount > 0)
);

CREATE UNIQUE INDEX uq_credit_grants_user_id ON credit_grants(user_id);

-- 14. Seed Initial Service Categories
INSERT INTO service_categories (id, name, icon_name, description, avg_lead_price)
VALUES
    ('it-software', 'IT & Dezvoltare Software', 'Code',
     'Magazine online, aplicații mobile, website-uri de prezentare și integrări API.', 35),
    ('constructii', 'Construcții & Amenajări', 'Hammer',
     'Renovări integrale, finisaje interioare, termoizolații și proiectare arhitecturală.', 30),
    ('contabilitate', 'Contabilitate & Fiscalitate', 'Calculator',
     'Evidență contabilă completă, declarații ANAF, audit financiar și salarizare.', 25),
    ('marketing', 'Marketing & Design Digital', 'TrendingUp',
     'Campanii Google Ads/Meta, identitate vizuală de brand, SEO și generare de lead-uri.', 30),
    ('instalatii', 'Instalații & Climatizare', 'Wrench',
     'Centrale termice, aer condiționat, pompe de căldură și intervenții electrice autorizate.', 20);
