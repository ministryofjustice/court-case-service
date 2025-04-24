BEGIN;
    alter table if exists defendant ADD COLUMN cpr_uuid TEXT;
END;