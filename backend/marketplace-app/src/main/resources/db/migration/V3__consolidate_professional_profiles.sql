-- One professional profile per account. JSON arrays retain multiple values without
-- comma-separated strings. Existing display labels are preserved, not guessed as IDs.
CREATE TABLE user_professional (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    categories JSONB NOT NULL DEFAULT '[]',
    services_offered JSONB NOT NULL DEFAULT '[]',
    coverage_areas JSONB NOT NULL DEFAULT '[]',
    capabilities JSONB NOT NULL DEFAULT '[]',
    rates JSONB NOT NULL DEFAULT '[]',
    supports_onsite BOOLEAN,
    supports_remote BOOLEAN,
    available_from DATE,
    available_until DATE,
    availability_confirmed_at TIMESTAMP WITH TIME ZONE,
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
        jsonb_typeof(categories) = 'array' AND jsonb_typeof(services_offered) = 'array'
        AND jsonb_typeof(coverage_areas) = 'array' AND jsonb_typeof(capabilities) = 'array'
        AND jsonb_typeof(rates) = 'array'),
    CONSTRAINT chk_professional_dates CHECK (available_until >= available_from),
    CONSTRAINT chk_professional_accepting CHECK (NOT accepting_leads OR onboarding_status = 'COMPLETE')
);

INSERT INTO user_professional (user_id, categories, services_offered, coverage_areas)
SELECT u.id,
    COALESCE((SELECT jsonb_agg(c.category_name ORDER BY c.category_name)
              FROM user_categories c WHERE c.user_id = u.id), '[]'::jsonb),
    COALESCE((SELECT jsonb_agg(s.service_name ORDER BY s.service_name)
              FROM user_services_offered s WHERE s.user_id = u.id), '[]'::jsonb),
    COALESCE((SELECT jsonb_agg(a.area ORDER BY a.area)
              FROM user_coverage_areas a WHERE a.user_id = u.id), '[]'::jsonb)
FROM users u
WHERE u.role = 'pro'
   OR EXISTS (SELECT 1 FROM user_categories c WHERE c.user_id = u.id)
   OR EXISTS (SELECT 1 FROM user_services_offered s WHERE s.user_id = u.id)
   OR EXISTS (SELECT 1 FROM user_coverage_areas a WHERE a.user_id = u.id);

-- Abort rather than remove a collection if any source value was not copied.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM user_categories c WHERE NOT EXISTS (
        SELECT 1 FROM user_professional p WHERE p.user_id=c.user_id AND p.categories ? c.category_name))
    OR EXISTS (SELECT 1 FROM user_services_offered s WHERE NOT EXISTS (
        SELECT 1 FROM user_professional p WHERE p.user_id=s.user_id AND p.services_offered ? s.service_name))
    OR EXISTS (SELECT 1 FROM user_coverage_areas a WHERE NOT EXISTS (
        SELECT 1 FROM user_professional p WHERE p.user_id=a.user_id AND p.coverage_areas ? a.area)) THEN
        RAISE EXCEPTION 'Professional migration did not preserve all collection values';
    END IF;
END $$;

DROP TABLE user_categories;
DROP TABLE user_services_offered;
DROP TABLE user_coverage_areas;

CREATE INDEX idx_professional_categories ON user_professional USING GIN (categories);
CREATE INDEX idx_professional_services ON user_professional USING GIN (services_offered);
CREATE INDEX idx_professional_coverage ON user_professional USING GIN (coverage_areas);
