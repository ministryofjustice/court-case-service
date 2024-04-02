BEGIN;
-- This and the following migration are to tidy up bad data that has been caused by the process unresulted case cron job
-- the cron used an incorrect method to checking if the hearing had been resulted

-- keep the first hearing outcome
DELETE FROM
    hearing_outcomes dup_ho
    USING hearing_outcomes dist_ho
WHERE dup_ho.id > dist_ho.id
  AND dup_ho.fk_hearing_defendant_id  = dist_ho.fk_hearing_defendant_id;


COMMIT;



