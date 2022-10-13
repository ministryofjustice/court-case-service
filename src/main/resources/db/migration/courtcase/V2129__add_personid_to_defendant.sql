BEGIN;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    alter table DEFENDANT add column PERSON_ID UUID NULL;

    update DEFENDANT SET PERSON_ID = uuid_generate_v4() where PERSON_ID is null;

COMMIT;
