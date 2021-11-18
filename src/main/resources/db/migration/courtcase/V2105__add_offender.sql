BEGIN;

    CREATE TABLE OFFENDER (
        ID                                  SERIAL PRIMARY KEY,
        CRN                                 TEXT UNIQUE,
        PREVIOUSLY_KNOWN_TERMINATION_DATE   DATE,
        PROBATION_STATUS                    TEXT,
        SUSPENDED_SENTENCE_ORDER            BOOLEAN NOT NULL DEFAULT FALSE,
        BREACH                              BOOLEAN NOT NULL DEFAULT FALSE,
        PRE_SENTENCE_ACTIVITY               BOOLEAN NOT NULL DEFAULT FALSE,
        AWAITING_PSR                        BOOLEAN,
        CREATED                             TIMESTAMP NOT NULL DEFAULT now(),
        CREATED_BY                          TEXT,
        LAST_UPDATED						TIMESTAMP,
        LAST_UPDATED_BY						TEXT,
        VERSION 							int4 NOT NULL DEFAULT 0,
        DELETED                             BOOLEAN NOT NULL DEFAULT FALSE
    );

    insert into OFFENDER ( CRN, PREVIOUSLY_KNOWN_TERMINATION_DATE, PROBATION_STATUS, SUSPENDED_SENTENCE_ORDER, BREACH, PRE_SENTENCE_ACTIVITY, AWAITING_PSR, CREATED_BY)
        select distinct crn, PREVIOUSLY_KNOWN_TERMINATION_DATE, PROBATION_STATUS, coalesce(SUSPENDED_SENTENCE_ORDER, false), coalesce(BREACH, false), coalesce(PRE_SENTENCE_ACTIVITY, FALSE), AWAITING_PSR, '(migration)'
        from DEFENDANT d ;

    ALTER TABLE DEFENDANT ADD CONSTRAINT fk_defendant_offender
                    FOREIGN KEY (CRN)
                    REFERENCES OFFENDER (CRN);
COMMIT;
