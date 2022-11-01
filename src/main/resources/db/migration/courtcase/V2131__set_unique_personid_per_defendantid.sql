BEGIN;

 UPDATE  DEFENDANT SET   person_id = uuid_generate_v4()
 FROM    DEFENDANT  AS d1
 WHERE   DEFENDANT.defendant_id = d1.defendant_id ;

COMMIT;
