BEGIN;
    alter table if exists case_defendant ADD UNIQUE (fk_court_case_id, fk_case_defendant_id);
    alter table if exists case_defendant_aud ADD UNIQUE (fk_court_case_id, fk_case_defendant_id);
END;