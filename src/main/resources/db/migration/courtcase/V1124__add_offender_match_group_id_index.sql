BEGIN;
create index offender_match_group_id_idx on offender_match (group_id);
COMMIT;
