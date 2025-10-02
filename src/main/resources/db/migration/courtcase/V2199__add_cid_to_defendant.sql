BEGIN;
    alter table if exists defendant ADD COLUMN c_id TEXT;
    alter table if exists defendant_aud ADD COLUMN c_id TEXT;
END;