BEGIN;

ALTER TABLE hearing_outcome
    ADD CONSTRAINT hearing_outcome_unique_hearing_defendant
        UNIQUE (fk_hearing_defendant_id);

COMMIT;