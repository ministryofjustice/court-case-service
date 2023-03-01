BEGIN;

ALTER TABLE OFFENCE ADD COLUMN PLEA_ID  INT NULL;


DROP TABLE IF EXISTS PLEA;

CREATE TABLE IF NOT EXISTS PLEA
(
    ID                                     SERIAL       PRIMARY KEY,
    PLEA_VALUE                             TEXT         NULL,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0
);

COMMIT;
