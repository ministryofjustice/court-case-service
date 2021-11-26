BEGIN;
-- In practice this field has never been populated and may be dropped entirely
alter table OFFENDER alter column SUSPENDED_SENTENCE_ORDER drop not null;

-- These fields *should* always be populated but aren't in practice. See PIC-1928 for details.
alter table OFFENDER alter column BREACH drop not null;
alter table OFFENDER alter column PRE_SENTENCE_ACTIVITY drop not null;

COMMIT;
