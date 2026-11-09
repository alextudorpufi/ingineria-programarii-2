-- Remove speculative matching inputs. V3 is already applied and must stay unchanged.
ALTER TABLE user_professional
    DROP CONSTRAINT chk_professional_arrays,
    DROP CONSTRAINT chk_professional_dates,
    DROP COLUMN rates,
    DROP COLUMN available_from,
    DROP COLUMN available_until,
    DROP COLUMN availability_confirmed_at,
    ADD CONSTRAINT chk_professional_arrays CHECK (
        jsonb_typeof(categories) = 'array'
        AND jsonb_typeof(services_offered) = 'array'
        AND jsonb_typeof(coverage_areas) = 'array'
        AND jsonb_typeof(capabilities) = 'array'
    );
