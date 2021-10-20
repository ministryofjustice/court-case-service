BEGIN;

alter table offender_match_group drop column court_code;
alter table offender_match_group drop column case_no;

COMMIT;
