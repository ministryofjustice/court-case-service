BEGIN;

ALTER TABLE hearing DROP CONSTRAINT hearing_id_unique_key;

COMMIT;
