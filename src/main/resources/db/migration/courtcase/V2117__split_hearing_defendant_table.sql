BEGIN;

-- Backup table
CREATE TABLE PIC_2024_HEARING_DEFENDANT AS TABLE DEFENDANT;

-- Split Table
CREATE TABLE HEARING_DEFENDANT AS TABLE DEFENDANT;
ALTER TABLE HEARING_DEFENDANT ADD PRIMARY KEY (id);

-- Update OFFENCE foreign key to point to HEARING_DEFENDANT
ALTER TABLE OFFENCE DROP CONSTRAINT fk_offence_hearing_defendant;
ALTER TABLE OFFENCE ADD CONSTRAINT fk_offence_hearing_defendant FOREIGN KEY(fk_hearing_defendant_id) REFERENCES HEARING_DEFENDANT(id);

-- Drop redundant columns from HEARING_DEFENDANT
ALTER TABLE HEARING_DEFENDANT
    DROP COLUMN defendant_name,
    DROP COLUMN type,
    DROP COLUMN name,
    DROP COLUMN address,
    DROP COLUMN crn,
    DROP COLUMN pnc,
    DROP COLUMN cro,
    DROP COLUMN date_of_birth,
    DROP COLUMN sex,
    DROP COLUMN nationality_1,
    DROP COLUMN nationality_2,
    DROP COLUMN manual_update,
    DROP COLUMN offender_confirmed;

-- Drop redundant columns from DEFENDANT
ALTER TABLE DEFENDANT
    DROP COLUMN fk_hearing_id;

-- Create sequences and constraints

CREATE SEQUENCE hearing_defendant_id_seq OWNED BY HEARING_DEFENDANT.id;
select setval('hearing_defendant_id_seq', max(id)) from HEARING_DEFENDANT;
ALTER TABLE HEARING_DEFENDANT ALTER COLUMN id SET DEFAULT nextval('hearing_defendant_id_seq');
CREATE INDEX defendant_defendant_id_key ON defendant(defendant_id);
ALTER TABLE HEARING_DEFENDANT ADD CONSTRAINT fk_hearing_defendant_hearing FOREIGN KEY(fk_hearing_id) REFERENCES HEARING(id);
CREATE INDEX hearing_defendant_hearing_id_idx on HEARING_DEFENDANT (fk_hearing_id);

COMMIT;
