BEGIN;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    alter table DEFENDANT add column UUID UUID not null;

    -- uuid_generate_v1 is based on MAC address of computer, timestamp and a random number, v4 on 3 random numbers
    update DEFENDANT SET UUID = uuid_generate_v4() where UUID is null;
COMMIT;
