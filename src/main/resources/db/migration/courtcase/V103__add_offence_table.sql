DROP TABLE IF EXISTS OFFENCE;

CREATE TABLE OFFENCE (
	ID                      SERIAL          PRIMARY KEY,
	ACT                     TEXT            NULL,
	OFFENCE_SUMMARY         TEXT            NULL,
	OFFENCE_TITLE           TEXT            NULL,
	SEQUENCE_NUMBER         int4            NULL,
	CASE_ID                 TEXT            NOT NULL,
    CONSTRAINT fk_court_case_court FOREIGN KEY (CASE_ID) REFERENCES COURT_CASE (CASE_ID)
);