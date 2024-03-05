BEGIN;

CREATE TABLE IF NOT EXISTS CASE_DEFENDANT
(
    ID                                     SERIAL       PRIMARY KEY,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0,
    FK_COURT_CASE_ID                       int4         NOT NULL,
    FK_CASE_DEFENDANT_ID                   int4         NOT NULL
);

CREATE TABLE IF NOT EXISTS CASE_DEFENDANT_DOCUMENTS
(
    ID                                     SERIAL       PRIMARY KEY,
    CREATED                                TIMESTAMP    NOT NULL DEFAULT now(),
    CREATED_BY                             TEXT         NULL,
    DELETED                                BOOLEAN      NOT NULL DEFAULT FALSE,
    LAST_UPDATED                           TIMESTAMP    NULL,
    LAST_UPDATED_BY                        TEXT         NULL,
    VERSION                                int4         NOT NULL DEFAULT 0,
    FK_CASE_DEFENDANT_ID                   int4         NOT NULL,
    DOCUMENT_ID                            UUID         NOT NULL,
    DOCUMENT_NAME                          TEXT         NULL
);

ALTER TABLE CASE_DEFENDANT ADD CONSTRAINT FK_CASE_DEFENDANT_COURT_CASE FOREIGN KEY (FK_COURT_CASE_ID) REFERENCES COURT_CASE;

CREATE INDEX CASE_DEFENDANT_FK_COURT_CASE_ID_IDX ON CASE_DEFENDANT(FK_COURT_CASE_ID);

ALTER TABLE CASE_DEFENDANT_DOCUMENTS ADD CONSTRAINT FK_CASE_DEFENDANT_DOCUMENTS_CASE_DEFENDANT FOREIGN KEY (FK_CASE_DEFENDANT_ID) REFERENCES CASE_DEFENDANT;

CREATE INDEX CASE_DEFENDANT_DOCUMENTS_FK_CASE_DEFENDANT_ID_IDX ON CASE_DEFENDANT_DOCUMENTS(FK_CASE_DEFENDANT_ID);

COMMIT;