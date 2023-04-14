BEGIN;

ALTER TABLE HEARING ADD COLUMN HEARING_OUTCOME_ID INT NULL;

CREATE TABLE IF NOT EXISTS HEARING_OUTCOME
(
    ID                                     SERIAL       PRIMARY KEY,
    OUTCOME_TYPE                           TEXT         NULL,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0
);

COMMIT;
