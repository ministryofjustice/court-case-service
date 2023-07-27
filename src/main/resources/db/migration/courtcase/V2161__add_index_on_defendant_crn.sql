BEGIN;

create index defendant_crn_idx on defendant (crn);

COMMIT;
