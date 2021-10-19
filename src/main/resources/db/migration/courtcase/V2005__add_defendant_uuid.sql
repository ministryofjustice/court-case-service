BEGIN;
    drop  extension IF exists "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    alter table DEFENDANT add column DEFENDANT_ID UUID;

    -- uuid_generate_v4 on 3 random numbers, uuid_generate_v1 is based on MAC address of computer, timestamp and a random number,
    update DEFENDANT SET DEFENDANT_ID = uuid_generate_v4() where DEFENDANT_ID is null;

    ALTER TABLE DEFENDANT ALTER COLUMN DEFENDANT_ID SET NOT NULL;
COMMIT;
