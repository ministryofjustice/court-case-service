BEGIN;

-- Backup table
CREATE TABLE PIC_2024_HEARING_DEFENDANT AS TABLE HEARING_DEFENDANT;

-- Split Table
CREATE TABLE DEFENDANT AS TABLE HEARING_DEFENDANT;
ALTER TABLE DEFENDANT ADD PRIMARY KEY (id);

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

CREATE SEQUENCE defendant_id_seq OWNED BY DEFENDANT.id;
select setval('defendant_id_seq', max(id)) from DEFENDANT;
ALTER TABLE DEFENDANT ALTER COLUMN id SET DEFAULT nextval('defendant_id_seq');
ALTER TABLE DEFENDANT ALTER COLUMN manual_update SET DEFAULT FALSE;
ALTER TABLE DEFENDANT ALTER COLUMN manual_update SET NOT NULL;
ALTER TABLE DEFENDANT ALTER COLUMN offender_confirmed SET DEFAULT FALSE;
ALTER TABLE DEFENDANT ALTER COLUMN offender_confirmed SET NOT NULL;
CREATE INDEX defendant_defendant_id_key ON defendant(defendant_id);
CREATE INDEX hearing_defendant_hearing_id_idx on HEARING_DEFENDANT (fk_hearing_id);

COMMIT;
