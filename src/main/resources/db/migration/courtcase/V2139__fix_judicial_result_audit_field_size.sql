BEGIN;
    ALTER TABLE IF EXISTS judicial_result_aud ALTER COLUMN label TYPE TEXT;
    ALTER TABLE IF EXISTS judicial_result_aud ALTER COLUMN judicial_result_type_id TYPE TEXT;
COMMIT;
