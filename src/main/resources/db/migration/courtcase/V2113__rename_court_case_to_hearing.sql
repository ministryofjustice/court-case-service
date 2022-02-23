BEGIN;

-- Update name of fields to be more descriptive
ALTER TABLE HEARING_DAY RENAME COLUMN court_case_id TO fk_hearing_id;
ALTER TABLE HEARING_DAY RENAME CONSTRAINT hearing_pkey TO hearing_day_pkey;
ALTER TABLE HEARING_DAY RENAME CONSTRAINT fk_hearing_court TO fk_hearing_day_court;
ALTER TABLE HEARING_DAY RENAME CONSTRAINT fk_hearing_court_case TO fk_hearing_day_court_case;
ALTER TABLE DEFENDANT RENAME COLUMN court_case_id TO fk_hearing_id;

CREATE TABLE PIC_2023_COURT_CASE_BK AS
  TABLE COURT_CASE;

-- Rename COURT_CASE table to retain existing constraints and references
ALTER TABLE COURT_CASE
  RENAME TO HEARING;
ALTER SEQUENCE hearing_id_seq
  RENAME TO hearing_day_id_seq;
ALTER SEQUENCE court_case_id_seq
  RENAME TO hearing_id_seq;

ALTER TABLE HEARING RENAME CONSTRAINT court_case_pkey TO hearing_pkey;

-- Then clone to recreate COURT_CASE table
CREATE TABLE COURT_CASE AS
  TABLE HEARING;
CREATE SEQUENCE court_case_id_seq OWNED BY COURT_CASE.id;

select setval('court_case_id_seq', max(id)) from COURT_CASE;

ALTER TABLE COURT_CASE ALTER COLUMN id SET DEFAULT nextval('court_case_id_seq');
ALTER TABLE COURT_CASE ADD PRIMARY KEY (id);

ALTER TABLE COURT_CASE
    ALTER COLUMN deleted SET DEFAULT FALSE,
    ALTER COLUMN CREATED SET DEFAULT now();

ALTER TABLE COURT_CASE
    ALTER COLUMN ID SET NOT NULL,
    ALTER COLUMN case_id SET NOT NULL,
    ALTER COLUMN deleted SET NOT NULL;

-- Update HEARING column names and drop ones no longer needed
ALTER TABLE HEARING
  RENAME COLUMN CASE_ID TO HEARING_ID;  -- for existing cases we use the CASE_ID as HEARING_ID to retain current behaviour

ALTER TABLE HEARING
  DROP COLUMN CASE_NO,
  DROP COLUMN SOURCE_TYPE;

-- Create foreign key relationship between HEARING and COURT_CASE
ALTER TABLE HEARING
  ADD COLUMN fk_court_case_id       INT8;

UPDATE HEARING SET fk_court_case_id = id;

ALTER TABLE HEARING
    ALTER COLUMN fk_court_case_id SET not null;

ALTER TABLE HEARING
    ADD CONSTRAINT fk_hearing_court_case FOREIGN KEY(fk_court_case_id) REFERENCES COURT_CASE(id);

create index hearing_day_court_code_idx on hearing_day (court_code);
create index hearing_created_idx on hearing (created);
COMMIT;
