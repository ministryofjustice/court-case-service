BEGIN;
    -- Rebuild the unique index on offender_match_group - needs more than just court_code and case_no now
    ALTER TABLE offender_match_group DROP CONSTRAINT offender_match_group_uq;
    CREATE UNIQUE INDEX offender_match_group_court_case_uq ON offender_match_group (COURT_CODE, CASE_NO, DEFENDANT_ID);
    ALTER TABLE offender_match_group ADD CONSTRAINT offender_match_group_uq UNIQUE USING INDEX offender_match_group_court_case_uq;
COMMIT;
