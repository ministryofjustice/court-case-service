BEGIN;
    alter table DEFENDANT add column PERSON_ID UUID NULL;
COMMIT;
