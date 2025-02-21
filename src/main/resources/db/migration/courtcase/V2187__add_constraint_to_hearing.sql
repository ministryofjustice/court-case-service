BEGIN;

ALTER TABLE IF EXISTS hearing ADD CONSTRAINT hearing_court_case_id_uniq UNIQUE (hearing_id, court_case_id);

COMMIT;
