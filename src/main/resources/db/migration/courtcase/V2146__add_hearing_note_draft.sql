BEGIN;

ALTER TABLE HEARING_NOTES ADD COLUMN DRAFT BOOLEAN DEFAULT false;

COMMIT;
