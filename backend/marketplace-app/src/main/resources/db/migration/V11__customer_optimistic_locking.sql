-- ==============================================================================
-- V11: Optimistic locking for customers.
--
-- Customer updates were last-write-wins because the row had no version column.
-- Existing rows start at 0; Hibernate increments the version on every update.
-- ==============================================================================

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
