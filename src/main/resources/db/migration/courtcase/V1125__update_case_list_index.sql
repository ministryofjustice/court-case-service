BEGIN;

ALTER TABLE COURT_CASE ADD COLUMN manual_update boolean DEFAULT false;
UPDATE COURT_CASE SET manual_update = CREATED_BY LIKE '%(prepare-a-case-for-court)';
ALTER TABLE COURT_CASE ALTER COLUMN manual_update SET not null;

COMMIT;
