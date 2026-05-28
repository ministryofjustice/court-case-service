BEGIN;

UPDATE offence_sfo_mapping
SET sfo_flag = FALSE
WHERE offence_code = 'CD71039';

COMMIT;