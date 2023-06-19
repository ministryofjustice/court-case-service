BEGIN;
-- This and the previous migration are to tidy up bad data that has accumulated in the offender_match and offender_match_group tables and
-- add appropriate constraints to prevent it happening again. The driver for this is https://dsdmoj.atlassian.net/browse/PIC-2975
-- which can't be completed until we've gotten rid of these invalid records.

--------- ISSUE 2 - Multiple offender_matches with same group_id and CRN ------------

-- Delete all but one record from offender_match where the group_id and CRN are the same
delete from OFFENDER_MATCH omdelete using (
        -- ids and id of the newest duplicate for each group having duplicates
        select id, newest_id from (
            -- all records grouped by group_id and crn where there are duplicates, and the id of the newest copy
            select  id,
                    count(*) over (partition by group_id, crn) as copies,
                    max(id) over (partition by group_id, crn) as newest_id
            from offender_match om order by om.group_id) as copies_table where copies_table.copies > 1
        ) duplicates where duplicates.id != duplicates.newest_id and duplicates.id = omdelete.id;

-- Add constraints to prevent the  issue happening again
ALTER TABLE OFFENDER_MATCH ADD CONSTRAINT uq_group_id_crn UNIQUE (group_id, crn);
END;
