BEGIN;

ALTER TABLE HEARING_OUTCOME ADD COLUMN ASSIGNED_TO TEXT default null;

ALTER TABLE HEARING_OUTCOME ADD COLUMN ASSIGNED_TO_UUID UUID default null;

COMMIT;