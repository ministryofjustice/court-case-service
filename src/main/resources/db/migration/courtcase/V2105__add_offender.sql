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
        SELECT crn, PREVIOUSLY_KNOWN_TERMINATION_DATE, PROBATION_STATUS, coalesce(SUSPENDED_SENTENCE_ORDER, false) as SUSPENDED_SENTENCE_ORDER, coalesce(BREACH, false) as BREACH,
                coalesce(PRE_SENTENCE_ACTIVITY, FALSE) as PRE_SENTENCE_ACTIVITY, AWAITING_PSR, '(migration)'
        FROM defendant d
            INNER JOIN
                ( SELECT d2.crn inner_crn, MAX(d2.created) AS latest FROM defendant d2 where d2.crn is not null GROUP BY d2.crn )
            AS grouped_defendant
            ON grouped_defendant.inner_crn = d.crn
            AND grouped_defendant.latest = d.created;

    ALTER TABLE DEFENDANT ADD CONSTRAINT fk_defendant_offender
        FOREIGN KEY (CRN)
        REFERENCES OFFENDER (CRN);

COMMIT;
