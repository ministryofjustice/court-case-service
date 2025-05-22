BEGIN;
    alter table if exists defendant_aud ADD COLUMN cpr_uuid UUID;
END;