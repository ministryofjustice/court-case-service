BEGIN;
create index defendant_id_idx on offender_match_group (defendant_id);
COMMIT;
