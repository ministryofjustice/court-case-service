BEGIN;

ALTER TABLE court_case DROP COLUMN session_start_time;
ALTER TABLE court_case DROP COLUMN court_code;
ALTER TABLE court_case DROP COLUMN court_room;
ALTER TABLE court_case DROP COLUMN list_no;

COMMIT;
