BEGIN;

ALTER TABLE if exists hearing_aud ADD COLUMN court_case_id text ;

END;