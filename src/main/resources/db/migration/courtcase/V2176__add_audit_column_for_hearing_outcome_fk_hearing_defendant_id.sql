BEGIN;

ALTER TABLE if exists hearing_outcome_aud ADD COLUMN IF NOT EXISTS fk_hearing_defendant_id integer;

ALTER TABLE if exists hearing_outcome_aud ADD COLUMN IF NOT EXISTS LEGACY boolean NOT NULL DEFAULT FALSE;

ALTER TABLE hearing_outcome_aud ADD CONSTRAINT fk_hearing_defendant_hearing_outcome FOREIGN KEY (fk_hearing_defendant_id) REFERENCES hearing_defendant;

END;