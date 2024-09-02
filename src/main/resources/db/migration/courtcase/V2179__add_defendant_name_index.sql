BEGIN;

CREATE INDEX IF NOT EXISTS defendant_names
    ON defendant (pnc, (name->>'forename1'), (name->>'surname'));

COMMIT;
