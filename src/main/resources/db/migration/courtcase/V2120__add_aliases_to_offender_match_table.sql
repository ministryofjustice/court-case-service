BEGIN;

ALTER TABLE OFFENDER_MATCH ADD COLUMN ALIASES JSONB;

COMMIT;
