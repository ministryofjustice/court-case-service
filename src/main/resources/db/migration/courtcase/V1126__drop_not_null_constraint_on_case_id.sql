BEGIN;

alter table court_case alter case_id drop not null;

COMMIT;
