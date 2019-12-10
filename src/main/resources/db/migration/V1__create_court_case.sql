DROP TABLE IF EXISTS COURT;

CREATE TABLE IF NOT EXISTS COURT
(
    ID                                     SERIAL       PRIMARY KEY,
    NAME                                   TEXT         NOT NULL
);


DROP TABLE IF EXISTS COURT_CASE;

CREATE TABLE IF NOT EXISTS COURT_CASE
(
  ID                                     SERIAL       PRIMARY KEY,
  CASE_ID                                BIGINT       NOT NULL,
  CASE_NO                                TEXT         NOT NULL,
  COURT_ID                               BIGINT       NOT NULL,
  COURT_ROOM                             TEXT         NULL,
  SESSION_START_DATE                     TIMESTAMP    NOT NULL,
  DATA                                   JSONB        NOT NULL,
  CONSTRAINT court_case_case_id_idempotent UNIQUE (CASE_ID),
  CONSTRAINT fk_court_case_court FOREIGN KEY (COURT_ID) REFERENCES COURT (ID)
);


