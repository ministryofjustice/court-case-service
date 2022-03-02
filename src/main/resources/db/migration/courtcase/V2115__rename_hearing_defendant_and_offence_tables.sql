BEGIN;

-- Back up tables
CREATE TABLE PIC_2024_DEFENDANT AS TABLE DEFENDANT;
CREATE TABLE PIC_2024_DEFENDANT_OFFENCE_BK AS TABLE DEFENDANT_OFFENCE;

-- Rename DEFENDANT -> HEARING_DEFENDANT
ALTER TABLE DEFENDANT RENAME TO HEARING_DEFENDANT;
ALTER SEQUENCE defendant_id_seq RENAME TO hearing_defendant_id_seq;
ALTER INDEX defendant_court_case_id_idx RENAME TO hearing_defendant_hearing_fk_hearing_id_idx;
ALTER INDEX defendant_crn_idx RENAME TO hearing_defendant_crn_idx;
ALTER TABLE HEARING_DEFENDANT RENAME CONSTRAINT defendant_pkey TO hearing_defendant_pkey;
ALTER TABLE HEARING_DEFENDANT RENAME CONSTRAINT fk_defendant_court_case TO fk_hearing_defendant_hearing;
ALTER TABLE HEARING_DEFENDANT RENAME CONSTRAINT fk_defendant_offender TO fk_hearing_defendant_offender;

-- Rename DEFENDANT_OFFENCE -> OFFENCE
ALTER TABLE DEFENDANT_OFFENCE RENAME TO OFFENCE;
ALTER SEQUENCE defendant_offence_id_seq RENAME TO offence_id_seq;
ALTER INDEX defendant_offence_defendant_id_idx RENAME TO offence_hearing_defendant_id_idx;
ALTER TABLE OFFENCE RENAME CONSTRAINT defendant_offence_pkey TO offence_pkey;
ALTER TABLE OFFENCE RENAME CONSTRAINT fk_defendant_offence_defendant TO fk_offence_hearing_defendant;

ALTER TABLE OFFENCE RENAME COLUMN DEFENDANT_ID to FK_HEARING_DEFENDANT_ID;

COMMIT;
