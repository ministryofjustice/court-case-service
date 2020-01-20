DROP TABLE IF EXISTS COURT;

CREATE TABLE IF NOT EXISTS COURT
(
    ID                                     SERIAL       PRIMARY KEY,
    NAME                                   TEXT         NOT NULL,
    COURT_CODE                             TEXT         NOT NULL,
    CONSTRAINT court_court_code_idempotent UNIQUE (COURT_CODE)
);


DROP TABLE IF EXISTS COURT_CASE;

CREATE TABLE IF NOT EXISTS COURT_CASE
(
  ID                                     SERIAL       PRIMARY KEY,
  CASE_ID                                TEXT         NOT NULL,
  CASE_NO                                TEXT         NOT NULL,
  COURT_CODE                             TEXT         DEFAULT 'SHF' NOT NULL,
  COURT_ROOM                             TEXT         NULL,
  SESSION_START_TIME                     TIMESTAMP    NOT NULL,
  PROBATION_STATUS                       TEXT         NOT NULL,
  DATA                                   JSONB        NOT NULL,
  CONSTRAINT court_case_case_id_idempotent UNIQUE (CASE_ID),
  CONSTRAINT fk_court_case_court FOREIGN KEY (COURT_CODE) REFERENCES COURT (COURT_CODE),
  CONSTRAINT court_case_case_no_idempotent UNIQUE (CASE_NO)
);


