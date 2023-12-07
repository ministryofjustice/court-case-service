BEGIN;

    ALTER TABLE  hearing_outcome ADD COLUMN fk_hearing_id integer ;

    CREATE INDEX  hearing_outcome_fk_hearing_id_idx on  hearing_outcome (fk_hearing_id);

    ALTER TABLE  hearing_outcome ADD CONSTRAINT hearing_outcome_unique_fk_hearing_id UNIQUE (fk_hearing_id);

    ALTER TABLE  hearing_outcome ADD CONSTRAINT fk_hearing_hearing_outcome FOREIGN KEY (fk_hearing_id) REFERENCES  hearing;

    UPDATE  hearing_outcome SET fk_hearing_id = h.id FROM  hearing h
        WHERE h.fk_hearing_outcome IS NOT NULL and  hearing_outcome.id = h.fk_hearing_outcome;

    ALTER TABLE hearing drop constraint fk_hearing_outcome;

END;