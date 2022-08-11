BEGIN;

create index case_id_idx on court_case (case_id);

CREATE TABLE CASE_COMMENTS (
   ID               SERIAL      PRIMARY KEY,
   CASE_ID          TEXT        NOT NULL,
   COMMENT          TEXT        NOT NULL,
   AUTHOR           TEXT        NOT NULL,
   CREATED          TIMESTAMP   NOT NULL DEFAULT now(),
   CREATED_BY       TEXT        NULL,
   CREATED_BY_UUID  TEXT        NOT NULL,
   LAST_UPDATED     TIMESTAMP   NOT NULL DEFAULT now(),
   LAST_UPDATED_BY  TEXT        NULL,
   DELETED          BOOLEAN     NOT NULL DEFAULT FALSE,
   VERSION          int4        NOT NULL DEFAULT 0
);

create index case_comment_case_id_idx on CASE_COMMENTS (case_id);

COMMIT;
