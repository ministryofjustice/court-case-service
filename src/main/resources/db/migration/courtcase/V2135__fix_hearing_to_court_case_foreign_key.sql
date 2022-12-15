BEGIN;

ALTER TABLE hearing DROP CONSTRAINT IF EXISTS fk_hearing_court_case;
ALTER TABLE hearing ADD CONSTRAINT fk_hearing_court_case FOREIGN KEY (fk_court_case_id) REFERENCES court_case(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS fk_hearing_court_case_id_idx on hearing (fk_court_case_id);
CREATE INDEX IF NOT EXISTS judicial_result_offence_id_idx on judicial_result (offence_id);

COMMIT;
