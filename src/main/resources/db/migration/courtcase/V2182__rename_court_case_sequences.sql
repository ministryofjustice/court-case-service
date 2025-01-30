BEGIN;

ALTER SEQUENCE IF EXISTS court_case_id_seq RENAME TO court_case_old_id_seq;
ALTER SEQUENCE IF EXISTS court_case_new_id_seq RENAME TO court_case_id_seq;

COMMIT;
