BEGIN;

ALTER TABLE COURT_CASE ADD COLUMN manual_update boolean DEFAULT false;
UPDATE COURT_CASE SET manual_update = false;
UPDATE COURT_CASE SET manual_update = true where CREATED_BY LIKE '%(prepare-a-case-for-court)';
ALTER TABLE COURT_CASE ALTER COLUMN manual_update SET not null;

drop index court_case_list_idx;
create index court_case_list_idx on court_case (created, court_code, manual_update);

COMMIT;
