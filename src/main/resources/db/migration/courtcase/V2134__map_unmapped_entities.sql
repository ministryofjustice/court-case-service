BEGIN;

    ALTER TABLE HEARING_DEFENDANT ADD COLUMN FK_DEFENDANT_ID int8;

    UPDATE HEARING_DEFENDANT hdu SET FK_DEFENDANT_ID = d.id FROM DEFENDANT d, HEARING_DEFENDANT hd
    WHERE hd.DEFENDANT_ID = d.DEFENDANT_ID
    AND hdu.id = hd.id;

    ALTER TABLE HEARING_DEFENDANT ADD CONSTRAINT FK_HEARING_DEFENDANT_DEFENDANT FOREIGN KEY (FK_DEFENDANT_ID) REFERENCES DEFENDANT;

    ALTER TABLE DEFENDANT ADD COLUMN FK_OFFENDER_ID int8;

    UPDATE DEFENDANT du SET FK_OFFENDER_ID = o.id FROM OFFENDER o, DEFENDANT d
    WHERE d.CRN IS NOT NULL
      AND d.CRN = o.CRN
      AND du.id = d.id;

    ALTER TABLE DEFENDANT ADD CONSTRAINT FK_DEFENDANT_OFFENDER FOREIGN KEY (FK_OFFENDER_ID) REFERENCES OFFENDER;

    ALTER TABLE hearing
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0;

    ALTER TABLE court_case
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0;

    ALTER TABLE defendant
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0,
        ADD COLUMN deleted bool NOT NULL DEFAULT false;

    ALTER TABLE hearing_defendant
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0,
        ADD COLUMN deleted bool NOT NULL DEFAULT false;

    ALTER TABLE hearing_day
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0,
        ADD COLUMN deleted bool NOT NULL DEFAULT false;

    ALTER TABLE offence
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0,
        ADD COLUMN deleted bool NOT NULL DEFAULT false;

    ALTER TABLE judicial_result
        ADD COLUMN last_updated timestamp NULL,
        ADD COLUMN last_updated_by text NULL,
        ADD COLUMN version int4 NOT NULL DEFAULT 0,
        ADD COLUMN deleted bool NOT NULL DEFAULT false;

COMMIT