BEGIN;
    ALTER TABLE COURT_CASE ADD COLUMN SOURCE_TYPE TEXT;
    UPDATE COURT_CASE SET SOURCE_TYPE = 'LIBRA' WHERE SOURCE_TYPE IS NULL;
    ALTER TABLE COURT_CASE ALTER COLUMN SOURCE_TYPE SET NOT NULL;
COMMIT;
