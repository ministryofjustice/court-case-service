BEGIN;

ALTER TABLE CASE_COMMENTS ADD COLUMN DEFENDANT_ID uuid NOT NULL;

DROP INDEX case_comment_case_id_idx;

CREATE INDEX case_comment_case_id_defendant_id_idx on CASE_COMMENTS(case_id, defendant_id);

COMMIT;