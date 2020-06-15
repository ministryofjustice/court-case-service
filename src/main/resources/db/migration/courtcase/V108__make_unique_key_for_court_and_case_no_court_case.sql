ALTER TABLE COURT_CASE DROP CONSTRAINT court_case_case_no_idempotent;

CREATE UNIQUE INDEX court_case_court_case_uq ON COURT_CASE (COURT_CODE, CASE_NO);

ALTER TABLE COURT_CASE ADD CONSTRAINT court_case_uq UNIQUE USING INDEX court_case_court_case_uq;
