BEGIN;

ALTER TABLE if exists hearing ADD COLUMN court_case_id TEXT;

END;