BEGIN;
create index court_case_list_idx on court_case (created, court_code);
drop index court_case_created_idx;
COMMIT;
