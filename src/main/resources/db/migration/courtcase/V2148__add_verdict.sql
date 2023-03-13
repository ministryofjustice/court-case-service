BEGIN;

ALTER TABLE OFFENCE ADD COLUMN VERDICT_ID  INT NULL;

DROP TABLE IF EXISTS VERDICT;

CREATE TABLE IF NOT EXISTS VERDICT
(
    ID                                     SERIAL       PRIMARY KEY,
    TYPE_DESCRIPTION                       TEXT         NULL,
    DATE                                   TIMESTAMP    NULL,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0
);

COMMIT;
