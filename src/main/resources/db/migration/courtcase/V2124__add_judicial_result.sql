BEGIN;
ALTER TABLE HEARING ADD COLUMN HEARING_EVENT_TYPE TEXT NULL;

DROP TABLE IF EXISTS JUDICIAL_RESULT;

CREATE TABLE IF NOT EXISTS JUDICIAL_RESULT
(
    ID                                     SERIAL       PRIMARY KEY,
    IS_CONVICTED_RESULT                    BOOLEAN      NULL DEFAULT FALSE,
    LABEL                                  TEXT         NULL,
    JUDICIAL_RESULT_TYPE_ID                TEXT         NULL,
    OFFENCE_ID                             INT8         NOT NULL ,
    JUDICIAL_RESULTS_ORDER                 INT4         NOT  NULL DEFAULT 0,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT            null,
    CONSTRAINT fk_offence FOREIGN KEY (OFFENCE_ID) REFERENCES OFFENCE (ID)
);

COMMIT;
