BEGIN;

CREATE INDEX IF NOT EXISTS hearing_defendant_fk_defendant_id_idx ON hearing_defendant (fk_defendant_id);

COMMIT;
