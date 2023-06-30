BEGIN;
-- This and the following migration are to tidy up bad data that has accumulated in the offender_match and offender_match_group tables and
-- add appropriate constraints to prevent it happening again. The driver for this is https://dsdmoj.atlassian.net/browse/PIC-2975
-- which can't be completed until we've gotten rid of these invalid records.


--------- ISSUE 1 - Unexpected null values in case_id and defendant_id fields ------------

    -- Cascade deletion from offender_match_group to offender_match
ALTER TABLE offender_match DROP CONSTRAINT fk_offender_match_group;
ALTER TABLE offender_match ADD CONSTRAINT fk_offender_match_group FOREIGN KEY(group_id) REFERENCES offender_match_group(id) ON DELETE CASCADE;

-- Drop any records where we have neither CASE_ID nor DEFENDANT_ID as we can't do anything with this information
DELETE FROM OFFENDER_MATCH_GROUP omg where omg.DEFENDANT_ID IS NULL and omg.CASE_ID IS NULL;

-- Find and update any records where we have a defendant_id but not case_id
UPDATE offender_match_group omg SET case_id = to_update.case_id FROM
    (select omg_inner.id as id, cc.case_id from court_case cc
        join hearing h on h.fk_court_case_id = cc.id
        join hearing_defendant hd on hd.fk_hearing_id = h.id
        join defendant d on d.defendant_id = hd.defendant_id
        join offender_match_group omg_inner on text(omg_inner.defendant_id) = text(d.defendant_id)
        where omg_inner.case_id is null and omg_inner.defendant_id is not null ) as to_update where omg.id = to_update.id;


-- Add constraints to prevent the previous issue happening again
ALTER TABLE OFFENDER_MATCH_GROUP ALTER COLUMN CASE_ID SET NOT NULL;
ALTER TABLE OFFENDER_MATCH_GROUP ALTER COLUMN DEFENDANT_ID SET NOT NULL;

END;
