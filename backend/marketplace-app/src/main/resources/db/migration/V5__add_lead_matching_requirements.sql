-- Existing leads retain an unknown work mode; never infer it from their category or location.
ALTER TABLE leads
    ADD COLUMN work_mode VARCHAR(20),
    ADD COLUMN required_capabilities JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN preferred_capabilities JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD CONSTRAINT chk_lead_work_mode CHECK (work_mode IN ('ONSITE', 'REMOTE')),
    ADD CONSTRAINT chk_lead_required_capabilities CHECK (
        CASE WHEN jsonb_typeof(required_capabilities) = 'array'
            THEN jsonb_array_length(required_capabilities) <= 50 ELSE false END
    ),
    ADD CONSTRAINT chk_lead_preferred_capabilities CHECK (
        CASE WHEN jsonb_typeof(preferred_capabilities) = 'array'
            THEN jsonb_array_length(preferred_capabilities) <= 50 ELSE false END
    );
