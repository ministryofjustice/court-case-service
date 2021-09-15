BEGIN;
    ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN CASE_ID TEXT;
    ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN DEFENDANT_ID TEXT;

    -- Migrate existing records in offender_match_group so that they have case and defendant id
    UPDATE offender_match_group omg
    SET    case_id = t2.new_case_id ,
           defendant_id = t2.defendant_id
    FROM   offender_match_group  omg_from
    JOIN
        (
            select cc.case_no , cc.court_code , cc.case_id as new_case_id, defendant_id from court_case cc
            inner join
            (
                select max(group_cc.created) as max_created, group_cc.case_id, defendant.defendant_id from court_case group_cc
                INNER JOIN defendant ON (defendant.court_case_id = group_cc.id)
                group by case_id, defendant.defendant_id
            ) grouped_cases
            on cc.case_id = grouped_cases.case_id
            where cc.created = grouped_cases.max_created
            and cc.deleted = false
        ) t2
    ON t2.court_code = omg_from.court_code AND t2.case_no = omg_from.case_no
    WHERE  omg.case_no = omg_from.case_no
    and omg.court_code = omg_from.court_code;

    -- There should be a unique key on case id and defendant id as there was for court code and case no
    CREATE UNIQUE INDEX offender_match_group_uq1 ON OFFENDER_MATCH_GROUP (CASE_ID, DEFENDANT_ID);
    ALTER TABLE OFFENDER_MATCH_GROUP ADD CONSTRAINT offender_match_group_uq1 UNIQUE USING INDEX offender_match_group_uq1;
COMMIT;
