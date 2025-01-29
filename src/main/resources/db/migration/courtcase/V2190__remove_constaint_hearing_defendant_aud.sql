BEGIN;
    ALTER TABLE if exists hearing_outcome_aud DROP CONSTRAINT fk_hearing_defendant_hearing_outcome;
END;