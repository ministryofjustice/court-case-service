BEGIN;

    ALTER TABLE if exists hearing_outcome_aud ADD COLUMN fk_hearing_id integer ;

END;