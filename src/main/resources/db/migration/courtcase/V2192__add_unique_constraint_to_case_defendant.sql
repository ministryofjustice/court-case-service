BEGIN;
    alter table if exists case_defendant ADD CONSTRAINT uniq_court_case_case_defendant UNIQUE (fk_court_case_id, fk_case_defendant_id);
END;