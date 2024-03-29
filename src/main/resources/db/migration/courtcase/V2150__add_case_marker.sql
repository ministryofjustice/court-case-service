BEGIN;

DROP TABLE IF EXISTS CASE_MARKER;

CREATE TABLE IF NOT EXISTS CASE_MARKER
(
    ID                                     SERIAL       PRIMARY KEY,
    FK_COURT_CASE_ID                       INT8         NOT NULL,
    TYPE_DESCRIPTION                       TEXT         NOT NULL,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0,
    CONSTRAINT fk_case_marker_case FOREIGN KEY (FK_COURT_CASE_ID) REFERENCES COURT_CASE (ID)

);
COMMIT;
