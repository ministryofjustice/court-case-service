BEGIN;

ALTER TABLE IF EXISTS offence_aud DROP COLUMN IF EXISTS data_model_version;
ALTER TABLE IF EXISTS offence_aud DROP COLUMN IF EXISTS short_term_custody_predictor_score;

COMMIT;