-- ==============================================================================
-- V1: Initial Marketplace Schema and Seed Data
-- Consolidated baseline migration for pre-release database initialization
-- ==============================================================================

-- 1. Customers
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Service Categories & Subcategories
CREATE TABLE IF NOT EXISTS service_categories (
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

CREATE TABLE IF NOT EXISTS service_subcategories (
    id UUID PRIMARY KEY,
    category_id VARCHAR(100) NOT NULL REFERENCES service_categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subcategories_category_id ON service_subcategories(category_id);

-- 3. Users & Professional Profiles
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'pro', -- 'client', 'pro', 'admin'
    name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    title VARCHAR(255),
    phone VARCHAR(50),
    avatar_url VARCHAR(1024),
    bio TEXT,
    verified_badge BOOLEAN NOT NULL DEFAULT FALSE,
    rating NUMERIC(3, 2) NOT NULL DEFAULT 5.00,
    review_count INT NOT NULL DEFAULT 0,
    credits_balance INT NOT NULL DEFAULT 0,
    hourly_rate NUMERIC(10, 2),
    client_budget NUMERIC(12, 2),
    response_time VARCHAR(100) DEFAULT '< 15 minute',
    completed_jobs INT NOT NULL DEFAULT 0,
    trust_score NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    member_since VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100),
    CONSTRAINT chk_users_credits_non_negative CHECK (credits_balance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users (LOWER(email));

-- 4. Professional Coverage Areas, Offered Services & Categories
CREATE TABLE IF NOT EXISTS user_coverage_areas (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    area VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, area)
);

CREATE TABLE IF NOT EXISTS user_services_offered (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    service_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, service_name)
);

CREATE TABLE IF NOT EXISTS user_categories (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, category_name)
);

-- 5. Leads (Service Requests)
CREATE TABLE IF NOT EXISTS leads (
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
    client_is_verified BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(50) NOT NULL DEFAULT 'validat', -- 'validat', 'in_asteptare', 'atribuit', 'finalizat', 'disputat'

    -- Lead Passport (AI & Verification Quality)
    passport_completeness_score INT NOT NULL DEFAULT 85,
    passport_coherence_status VARCHAR(50) NOT NULL DEFAULT 'Excelentă',
    passport_contact_verified BOOLEAN NOT NULL DEFAULT TRUE,
    passport_intent_verified BOOLEAN NOT NULL DEFAULT TRUE,
    passport_required_deliverables TEXT,
    passport_clarifications_remaining TEXT,
    passport_extracted_by_ai BOOLEAN NOT NULL DEFAULT TRUE,
    passport_duplicate_risk VARCHAR(50) NOT NULL DEFAULT 'Scăzut (< 2%)',

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

    CONSTRAINT chk_leads_current_pros CHECK (current_pros >= 0 AND current_pros <= max_pros),
    CONSTRAINT chk_leads_budget_order CHECK (budget_min >= 0 AND budget_max >= budget_min),
    CONSTRAINT chk_leads_price_positive CHECK (lead_price > 0 AND max_pros > 0)
);

CREATE INDEX IF NOT EXISTS idx_leads_category ON leads(category_id);
CREATE INDEX IF NOT EXISTS idx_leads_client ON leads(client_id);
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
CREATE INDEX IF NOT EXISTS idx_leads_created_at ON leads(created_at);

-- 6. Claimed Pros on Leads
CREATE TABLE IF NOT EXISTS lead_claims (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    pro_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    pro_name VARCHAR(255) NOT NULL,
    claimed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_lead_claims_lead_pro UNIQUE (lead_id, pro_id)
);

CREATE INDEX IF NOT EXISTS idx_lead_claims_lead_id ON lead_claims(lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_claims_pro_id ON lead_claims(pro_id);

-- 7. Wallet Transactions (Credits Integrity)
-- Append-only ledger. Application code must never UPDATE or DELETE.
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    type VARCHAR(50) NOT NULL, -- 'lead_purchase', 'refund', 'top_up', 'bonus'
    amount INT NOT NULL, -- positive or negative
    description VARCHAR(500) NOT NULL,
    balance_after INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    CONSTRAINT chk_wallet_balance_non_negative CHECK (balance_after >= 0)
);

CREATE INDEX IF NOT EXISTS idx_wallet_tx_user_id ON wallet_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_lead_id ON wallet_transactions(lead_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_created_at ON wallet_transactions(created_at);

-- 8. Disputes & Refund Ledger
CREATE TABLE IF NOT EXISTS disputes (
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
    CONSTRAINT chk_disputes_credits_positive CHECK (credits_requested > 0)
);

CREATE INDEX IF NOT EXISTS idx_disputes_lead_id ON disputes(lead_id);
CREATE INDEX IF NOT EXISTS idx_disputes_pro_id ON disputes(pro_id);
CREATE INDEX IF NOT EXISTS idx_disputes_status ON disputes(status);

-- 9. Chat Messages
CREATE TABLE IF NOT EXISTS chat_messages (
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

CREATE INDEX IF NOT EXISTS idx_chat_messages_lead_id ON chat_messages(lead_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sent_at ON chat_messages(sent_at);

-- 10. Ratings (Verified Rating Protocol)
CREATE TABLE IF NOT EXISTS ratings (
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

CREATE INDEX IF NOT EXISTS idx_ratings_pro_id ON ratings(pro_id);
CREATE INDEX IF NOT EXISTS idx_ratings_lead_id ON ratings(lead_id);

-- 11. Database Audit Trail Ledger
-- Append-only ledger. Application code must never UPDATE or DELETE.
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'CLAIM_LEAD', 'CREDIT_DEDUCTION', 'REFUND_APPROVED'
    changed_by VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);

-- 12. Seed Initial Service Categories
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
     'Centrale termice, aer condiționat, pompe de căldură și intervenții electrice autorizate.', 20)
ON CONFLICT (id) DO NOTHING;
