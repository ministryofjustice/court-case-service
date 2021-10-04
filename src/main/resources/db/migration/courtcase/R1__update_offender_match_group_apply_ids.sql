BEGIN;
   -- Migrate existing records in offender_match_group so that they have case and defendant id
    UPDATE offender_match_group omg
    SET    case_id = t2.new_case_id ,
           defendant_id = t2.defendant_id,
           last_updated = now(),
           last_updated_by = '(migration)'

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
    WHERE omg.case_no = omg_from.case_no
    and omg.court_code = omg_from.court_code
    and omg.case_id is null
    and omg.defendant_id is null;

COMMIT;
