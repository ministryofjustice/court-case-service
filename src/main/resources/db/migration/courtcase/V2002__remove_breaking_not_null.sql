BEGIN;
    alter table DEFENDANT alter column SUSPENDED_SENTENCE_ORDER drop not null;
    alter table DEFENDANT alter column BREACH drop not null;
    alter table DEFENDANT alter column PRE_SENTENCE_ACTIVITY drop not null;
COMMIT;
