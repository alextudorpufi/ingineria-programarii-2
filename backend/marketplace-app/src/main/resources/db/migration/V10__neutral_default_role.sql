-- The domain always sets a role on registration; the database default only
-- matters for raw inserts. Default to the least-privileged role instead of
-- silently creating professionals.
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'client';
