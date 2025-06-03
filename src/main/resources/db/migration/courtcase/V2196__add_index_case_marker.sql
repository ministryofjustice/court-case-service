BEGIN;

CREATE INDEX IF NOT EXISTS case_marker_fk_court_case_id_idx ON case_marker (fk_court_case_id);

COMMIT;
