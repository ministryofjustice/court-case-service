BEGIN;

CREATE TABLE HEARING_NOTES (
   ID               SERIAL      PRIMARY KEY,
   HEARING_ID       TEXT        NOT NULL,
   NOTE             TEXT        NOT NULL,
   AUTHOR           TEXT        NOT NULL,
   CREATED          TIMESTAMP   NOT NULL DEFAULT now(),
   CREATED_BY       TEXT        NULL,
   CREATED_BY_UUID  UUID        NOT NULL,
   LAST_UPDATED     TIMESTAMP   NOT NULL DEFAULT now(),
   LAST_UPDATED_BY  TEXT        NULL,
   DELETED          BOOLEAN     NOT NULL DEFAULT FALSE,
   VERSION          int4        NOT NULL DEFAULT 0
);

create index hearing_notes_hearing_id_idx on HEARING_NOTES (hearing_id);

COMMIT;
