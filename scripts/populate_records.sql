-- Shared development-only seed. Invoke through one of the size wrappers.
-- Deterministic IDs, insert-only: existing accounts, balances and profiles survive.
-- Synthetic wallet/history data is for UI/load testing, not ledger reconciliation.
\set ON_ERROR_STOP on
BEGIN;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO service_categories (id, name, icon_name, description, avg_lead_price, version, created_at, created_by)
SELECT
    'cat-' || LPAD(i::text, GREATEST(5, length(i::text)), '0') AS id,
    'Serviciu Specializat #' || i AS name,
    CASE (i % 5)
        WHEN 0 THEN 'Code'
        WHEN 1 THEN 'Hammer'
        WHEN 2 THEN 'Calculator'
        WHEN 3 THEN 'TrendingUp'
        ELSE 'Wrench'
    END AS icon_name,
    'Descriere pentru serviciul specializat numărul ' || i AS description,
    20 + (i % 40) AS avg_lead_price,
    0,
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(6, :record_count) AS i
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO service_subcategories (id, category_id, name, created_at)
SELECT
    ('00000000-0000-0000-0004-' || LPAD(i::text, 12, '0'))::uuid,
    CASE
        WHEN (i % :record_count) < 5
            THEN (ARRAY['it-software', 'constructii', 'contabilitate', 'marketing', 'instalatii'])[(i % :record_count) + 1]
        ELSE 'cat-' || LPAD(((i % :record_count) + 1)::text, GREATEST(5, length(((i % :record_count) + 1)::text)), '0')
    END,
    'Subcategorie Specializată #' || i,
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO customers (id, name, email, phone, created_at)
SELECT
    ('00000000-0000-0000-0005-' || LPAD(i::text, 12, '0'))::uuid,
    'Client Persoană Fizică ' || i,
    'customer_' || i || '@marketplace.ro',
    '+40 7' || LPAD((20000000 + i)::text, 8, '0'),
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
--    - 1 ADMIN:   skillingadmin@contact.ro       / pass: admin
-- ------------------------------------------------------------------------------

-- Root administrator (existing credentials and edits are preserved)
INSERT INTO users (
    id, email, password_hash, role, name, company_name, title, phone, avatar_url, bio,
    verified_badge, rating, review_count, credits_balance, hourly_rate, client_budget,
    response_time, completed_jobs, trust_score, member_since, version, created_at, created_by
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'skillingadmin@contact.ro',
    '$2a$10$yWQrlMUG3w.qLjSErNpEYOF0tLY7IHRKRQ0r/qqhumBbbn7i9YYMq',
    'admin',
    'Skillink Admin Master',
    'Skillink Platform Core HQ',
    'Super Administrator & Trust Officer',
    '+40 700 000 001',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150',
    'Contul administrativ principal pentru audit, mediere litigii și management sistem.',
    TRUE, 5.00, 100, 10000, 0.00, 0.00, '< 5 minute', 250, 100.00, 'Ianuarie 2024',
    0, NOW(), 'system'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO users (
    id, email, password_hash, role, name, company_name, title, phone, avatar_url, bio,
    verified_badge, rating, review_count, credits_balance, hourly_rate, client_budget,
    response_time, completed_jobs, trust_score, member_since, version, created_at, created_by
)
SELECT
    ('00000000-0000-0000-0001-' || LPAD(i::text, 12, '0'))::uuid,
    CASE WHEN i = 1 THEN 'skillingclient@contact.ro' ELSE 'client_' || i || '@skillink.ro' END,
    '$2a$10$yWQrlMUG3w.qLjSErNpEYOF0tLY7IHRKRQ0r/qqhumBbbn7i9YYMq',
    'client',
    CASE WHEN i = 1 THEN 'Andrei Popescu' ELSE 'Client Rezidențial ' || i END,
    CASE WHEN i % 3 = 0 THEN 'Compania Client ' || i || ' SRL' ELSE NULL END,
    'Beneficiar Proiect',
    CASE WHEN i = 1 THEN '+40 721 234 567' ELSE '+40 7' || LPAD((30000000 + i)::text, 8, '0') END,
    '',
    CASE WHEN i = 1 THEN 'Client activ cu proiecte rezidențiale și buget verificat.'
         ELSE 'Client activ pe platforma Skillink.' END,
    (i % 2 = 0),
    4.50 + ((i % 50)::numeric / 100),
    (i % 20),
    0,
    0.00,
    5000.00 + ((i * 37) % 50000),
    '< 30 minute',
    (i % 15),
    95.00 + ((i % 50)::numeric / 10),
    'Martie 2024',
    0,
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(1, :client_count) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (
    id, email, password_hash, role, name, company_name, title, phone, avatar_url, bio,
    verified_badge, rating, review_count, credits_balance, hourly_rate, client_budget,
    response_time, completed_jobs, trust_score, member_since, version, created_at, created_by
)
SELECT
    ('00000000-0000-0000-0002-' || LPAD(i::text, 12, '0'))::uuid,
    CASE WHEN i = 1 THEN 'skillingprof@contact.ro' ELSE 'pro_' || i || '@skillink.ro' END,
    '$2a$10$yWQrlMUG3w.qLjSErNpEYOF0tLY7IHRKRQ0r/qqhumBbbn7i9YYMq',
    'pro',
    CASE WHEN i = 1 THEN 'Mihai Ionescu' ELSE 'Meseriaș / Specialist ' || i END,
    CASE WHEN i = 1 THEN 'Ionescu Smart Systems SRL' ELSE 'Expert Services ' || i || ' SRL' END,
    'Specialist Autorizat ' || (CASE (i % 4) WHEN 0 THEN 'Electrician' WHEN 1 THEN 'Instalator' WHEN 2 THEN 'Constructor' ELSE 'Programator' END),
    '+40 7' || LPAD((40000000 + i)::text, 8, '0'),
    '',
    CASE WHEN i = 1 THEN 'Profesionist verificat cu experiență în automatizări, aplicații web și integrări.'
         ELSE 'Profesionist verificat cu experiență dovedită pe platformă.' END,
    TRUE,
    4.20 + ((i % 80)::numeric / 100),
    10 + (i % 150),
    50 + (i % 500),
    100.00 + ((i * 13) % 250),
    0.00,
    '< 15 minute',
    15 + (i % 200),
    90.00 + ((i % 100)::numeric / 10),
    'Februarie 2024',
    0,
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(1, :professional_count) AS i
ON CONFLICT (id) DO NOTHING;

-- Only populate generated professionals; preserve all existing profiles on reruns.
INSERT INTO user_professional (
    user_id, categories, services_offered, coverage_areas, capabilities,
    supports_onsite, supports_remote, onboarding_status, accepting_leads
)
SELECT id, '["it-software"]'::jsonb,
       '["Dezvoltare aplicații web"]'::jsonb, '["RO-B", "RO-IF"]'::jsonb,
       '["Aplicații web", "Integrare API"]'::jsonb,
       TRUE, TRUE, 'COMPLETE', TRUE
FROM users
WHERE role = 'pro' AND id IN (
    SELECT ('00000000-0000-0000-0002-' || LPAD(i::text, 12, '0'))::uuid
    FROM generate_series(1, :professional_count) AS i
)
ON CONFLICT (user_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO leads (
    id, title, category_id, subcategory, description, location,
    work_mode, required_capabilities, preferred_capabilities,
    budget_min, budget_max, budget_currency, budget_is_negotiable, budget_is_confirmed,
    timeline, lead_price, max_pros, current_pros,
    client_id, client_name, client_phone, client_email, client_city, client_is_verified,
    status, passport_completeness_score, passport_coherence_status, passport_contact_verified,
    passport_intent_verified, passport_extracted_by_ai, passport_duplicate_risk,
    version, created_at, created_by
)
SELECT
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS id,
    'Proiect Execuție Lucrare #' || i AS title,
    -- Keep the five human-facing root categories evenly populated so the
        (ARRAY['it-software', 'constructii', 'contabilitate', 'marketing', 'instalatii'])[((i - 1) % 5) + 1] AS category_id,
    'Subcategorie #' || ((i % 100) + 1) AS subcategory,
    'Solicit ofertă detaliată pentru lucrarea cu numărul ' || i || '. Cerințele includ materiale de calitate superioară și garanție standard.',
    CASE (i % 5)
        WHEN 0 THEN 'București, Sector 1'
        WHEN 1 THEN 'Cluj-Napoca, Centru'
        WHEN 2 THEN 'Timișoara'
        WHEN 3 THEN 'Iași'
        ELSE 'Brașov'
    END AS location,
    CASE WHEN (i % 5) IN (0, 3, 4) THEN 'REMOTE' ELSE 'ONSITE' END,
    jsonb_build_array((ARRAY['Aplicații web', 'Renovări interioare', 'Contabilitate SRL', 'Google Ads', 'Instalații sanitare'])[((i - 1) % 5) + 1]),
    jsonb_build_array('Consultanță'),
    1000.00 + ((i * 17) % 5000) AS budget_min,
    6500.00 + ((i * 23) % 15000) AS budget_max,
    'RON',
    TRUE,
    TRUE,
    'În următoarele 2 săptămâni',
    20 + (i % 30) AS lead_price,
    4 AS max_pros,
    1 AS current_pros,
    ('00000000-0000-0000-0001-' || LPAD(((i % :client_count) + 1)::text, 12, '0'))::uuid AS client_id,
    CASE WHEN ((i % :client_count) + 1) = 1 THEN 'Andrei Popescu'
         ELSE 'Client Rezidențial ' || ((i % :client_count) + 1) END,
    CASE WHEN ((i % :client_count) + 1) = 1 THEN '+40 721 234 567'
         ELSE '+40 7' || LPAD((30000000 + ((i % :client_count) + 1))::text, 8, '0') END,
    CASE WHEN ((i % :client_count) + 1) = 1 THEN 'skillingclient@contact.ro'
         ELSE 'client_' || ((i % :client_count) + 1) || '@skillink.ro' END,
    'București',
    TRUE,
    CASE (i % 4)
        WHEN 0 THEN 'validat'
        WHEN 1 THEN 'in_asteptare'
        WHEN 2 THEN 'atribuit'
        ELSE 'finalizat'
    END AS status,
    80 + (i % 20),
    'Excelentă',
    TRUE,
    TRUE,
    TRUE,
    'Scăzut (< 2%)',
    0,
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(1, :record_count) AS i
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO lead_claims (id, lead_id, pro_id, pro_name, claimed_at)
SELECT
    ('00000000-0000-0000-0006-' || LPAD(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS lead_id,
    ('00000000-0000-0000-0002-' || LPAD(((i % :professional_count) + 1)::text, 12, '0'))::uuid AS pro_id,
    CASE WHEN ((i % :professional_count) + 1) = 1 THEN 'Mihai Ionescu'
         ELSE 'Meseriaș / Specialist ' || ((i % :professional_count) + 1) END,
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO wallet_transactions (id, user_id, lead_id, type, amount, description, balance_after, created_at, created_by)
SELECT
    ('00000000-0000-0000-0007-' || LPAD(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0002-' || LPAD(((i % :professional_count) + 1)::text, 12, '0'))::uuid AS user_id,
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS lead_id,
    CASE (i % 3)
        WHEN 0 THEN 'lead_purchase'
        WHEN 1 THEN 'top_up'
        ELSE 'bonus'
    END AS type,
    CASE (i % 3)
        WHEN 0 THEN -30
        WHEN 1 THEN 100
        ELSE 25
    END AS amount,
    'Tranzacție sistem pentru lead/alimentare sold #' || i,
    150 + (i % 200),
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO disputes (
    id, lead_id, lead_title, pro_id, pro_name, reason, details,
    credits_requested, status, admin_decision_note, resolved_at,
    version, created_at, created_by
)
SELECT
    ('00000000-0000-0000-0008-' || LPAD(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS lead_id,
    'Proiect Execuție Lucrare #' || i AS lead_title,
    ('00000000-0000-0000-0002-' || LPAD(((i % :professional_count) + 1)::text, 12, '0'))::uuid AS pro_id,
    CASE WHEN ((i % :professional_count) + 1) = 1 THEN 'Mihai Ionescu'
         ELSE 'Meseriaș / Specialist ' || ((i % :professional_count) + 1) END,
    CASE (i % 4)
        WHEN 0 THEN 'unreachable_contact'
        WHEN 1 THEN 'incompatible_budget'
        WHEN 2 THEN 'wrong_category'
        ELSE 'duplicate_or_fake'
    END AS reason,
    'Detalii sesizare înregistrată automat pentru investigație #' || i,
    30 AS credits_requested,
    CASE (i % 3)
        WHEN 0 THEN 'in_analiza'
        WHEN 1 THEN 'aprobat'
        ELSE 'respins'
    END AS status,
    CASE WHEN (i % 3) <> 0 THEN 'Decizie validată conform protocolului de arbitraj.' ELSE NULL END,
    CASE WHEN (i % 3) <> 0 THEN NOW() - (i || ' minutes')::interval ELSE NULL END,
    0,
    NOW() - (i || ' minutes')::interval,
    'system'
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO chat_messages (id, lead_id, sender_id, sender_name, sender_role, content, is_offer, offer_amount, sent_at)
SELECT
    ('00000000-0000-0000-0009-' || LPAD(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS lead_id,
    ('00000000-0000-0000-0002-' || LPAD(((i % :professional_count) + 1)::text, 12, '0'))::uuid AS sender_id,
    CASE WHEN ((i % :professional_count) + 1) = 1 THEN 'Mihai Ionescu'
         ELSE 'Meseriaș / Specialist ' || ((i % :professional_count) + 1) END,
    'pro',
    'Bună ziua! Am analizat cererea dvs. #' || i || ' și sunt disponibil să încep lucrarea săptămâna viitoare.',
    (i % 5 = 0),
    CASE WHEN (i % 5 = 0) THEN 3500.00 ELSE NULL END,
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO ratings (
    id, pro_id, lead_id, lead_title, client_name,
    score_timeliness, score_quality, score_communication, score_budget_respect,
    overall_score, comment, verified_status, created_at
)
SELECT
    ('00000000-0000-0000-000a-' || LPAD(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0002-' || LPAD(((i % :professional_count) + 1)::text, 12, '0'))::uuid AS pro_id,
    ('00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'))::uuid AS lead_id,
    'Proiect Execuție Lucrare #' || i,
    CASE WHEN ((i % :client_count) + 1) = 1 THEN 'Andrei Popescu'
         ELSE 'Client Rezidențial ' || ((i % :client_count) + 1) END,
    4 + (i % 2),
    4 + (i % 2),
    5,
    4 + (i % 2),
    4.80,
    'Serviciu de excepție, comunicare promptă și respectarea termenelor pentru lucrarea #' || i,
    TRUE,
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
INSERT INTO audit_logs (id, entity_name, entity_id, action, changed_by, details, created_at)
SELECT
    ('00000000-0000-0000-000b-' || LPAD(i::text, 12, '0'))::uuid,
    'Lead',
    '00000000-0000-0000-0003-' || LPAD(i::text, 12, '0'),
    CASE (i % 4)
        WHEN 0 THEN 'CREATE'
        WHEN 1 THEN 'STATUS_CHANGE'
        WHEN 2 THEN 'CLAIM_LEAD'
        ELSE 'CREDIT_DEDUCTION'
    END,
    'skillingadmin@contact.ro',
    'Audit operațiune automatizată în sistem pentru înregistrarea #' || i,
    NOW() - (i || ' minutes')::interval
FROM generate_series(1, :record_count) AS i
ON CONFLICT DO NOTHING;

-- Refresh planner statistics for the newly inserted data
ANALYZE users;
ANALYZE leads;
ANALYZE lead_claims;
ANALYZE wallet_transactions;
ANALYZE disputes;
ANALYZE chat_messages;
ANALYZE ratings;
ANALYZE audit_logs;

COMMIT;
