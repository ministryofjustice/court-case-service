BEGIN;

ALTER TABLE HEARING_OUTCOME ADD COLUMN state TEXT NOT NULL default 'NEW';

create index hearing_outcome_state_idx on HEARING_OUTCOME (state);

COMMIT;
