BEGIN;

ALTER INDEX court_case_case_id_idx RENAME TO hearing_hearing_id_idx;
ALTER INDEX hearing_court_case_id_idx RENAME TO hearing_day_hearing_id_idx;

ALTER TABLE hearing ALTER COLUMN hearing_id SET NOT NULL;
ALTER TABLE hearing ALTER COLUMN created SET NOT NULL;

ALTER TABLE offender ALTER COLUMN last_updated SET DEFAULT NOW();
ALTER TABLE offender
    ALTER COLUMN crn SET NOT NULL,
    ALTER COLUMN probation_status SET NOT NULL,
    ALTER COLUMN last_updated SET NOT NULL;

COMMIT;
