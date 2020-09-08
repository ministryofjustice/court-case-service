
CREATE UNIQUE INDEX offender_match_group_court_case__uq ON offender_match_group (COURT_CODE, CASE_NO);

ALTER TABLE offender_match_group ADD CONSTRAINT offender_match_group_uq UNIQUE USING INDEX offender_match_group_court_case__uq;
