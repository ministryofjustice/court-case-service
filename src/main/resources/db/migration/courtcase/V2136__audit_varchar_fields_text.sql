BEGIN;

    ALTER TABLE offence_aud ALTER COLUMN summary TEXT;
    ALTER TABLE offence_aud ALTER COLUMN title TEXT;

COMMIT;
