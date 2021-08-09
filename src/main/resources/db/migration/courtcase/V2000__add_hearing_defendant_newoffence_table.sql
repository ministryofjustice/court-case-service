DROP TABLE IF EXISTS DEFENDANT_OFFENCE;
DROP TABLE IF EXISTS DEFENDANT;
DROP TABLE IF EXISTS HEARING;

CREATE TABLE HEARING (
	ID                  SERIAL       PRIMARY KEY,
	COURT_CASE_ID       INT8         NOT NULL,
    HEARING_DAY         DATE         NOT NULL,
    HEARING_TIME        TIME         NOT NULL,
    COURT_CODE          TEXT         NOT NULL,
    COURT_ROOM          TEXT         NOT NULL,
    LIST_NO             TEXT         NOT NULL,
    CREATED             TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY          TEXT         NULL,
    CONSTRAINT fk_hearing_court_case FOREIGN KEY (COURT_CASE_ID) REFERENCES COURT_CASE (ID)
);

CREATE TABLE DEFENDANT (
    ID                  SERIAL      PRIMARY KEY,
    COURT_CASE_ID       INT8        NOT NULL,
    DEFENDANT_NAME      TEXT        NOT NULL,
    TYPE                TEXT        NOT NULL DEFAULT 'PERSON',
    NAME                JSONB       NOT NULL,
    ADDRESS             JSONB       NULL,
    CRN                 TEXT        NULL,
    PNC                 TEXT        NULL,
    CRO                 TEXT        NULL,
    DATE_OF_BIRTH       DATE        NULL,
    SEX                 TEXT        NULL,
    NATIONALITY_1       TEXT        NULL,
    NATIONALITY_2       TEXT        NULL,
    CREATED             TIMESTAMP   NOT NULL DEFAULT now(),
    CREATED_BY          TEXT        NULL,
    CONSTRAINT fk_defendant_court_case FOREIGN KEY (COURT_CASE_ID) REFERENCES COURT_CASE (ID)
);

-- Expect that this table will be renamed to OFFENCE when the existing OFFENCE is retired and dropped
CREATE TABLE DEFENDANT_OFFENCE (
    ID              SERIAL      PRIMARY KEY,
    DEFENDANT_ID    INT8        NOT NULL,
    SUMMARY         TEXT        NOT NULL,
    TITLE           TEXT        NOT NULL,
    SEQUENCE        INT4        NOT NULL,
    ACT             TEXT        NULL,
    CREATED         TIMESTAMP   NOT NULL DEFAULT now(),
    CREATED_BY      TEXT        NULL,
    CONSTRAINT fk_defendant_offence_defendant FOREIGN KEY (DEFENDANT_ID) REFERENCES DEFENDANT (ID)
);
