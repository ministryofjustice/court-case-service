BEGIN;

ALTER TABLE offence DROP COLUMN IF EXISTS data_model_version;
ALTER TABLE offence DROP COLUMN IF EXISTS short_term_custody_predictor_score;

COMMIT;