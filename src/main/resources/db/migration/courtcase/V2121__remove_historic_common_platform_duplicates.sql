BEGIN;

-- DROP old backups

DROP TABLE IF EXISTS court_case_archive;
DROP TABLE IF EXISTS court_case_bk;
DROP TABLE IF EXISTS defendant_archive;
DROP TABLE IF EXISTS defendant_offence_archive;
DROP TABLE IF EXISTS hearing_archive;
DROP TABLE IF EXISTS offence_archive;
DROP TABLE IF EXISTS offence_bk;
DROP TABLE IF EXISTS offender_match_archive;
DROP TABLE IF EXISTS offender_match_group_archive;
DROP TABLE IF EXISTS pic_2018_court_case_bk;
DROP TABLE IF EXISTS pic_2018_hearing_bk;
DROP TABLE IF EXISTS pic_2023_court_case_bk;
DROP TABLE IF EXISTS pic_2024_hearing_defendant;
DROP TABLE IF EXISTS pic_2024_defendant;
DROP TABLE IF EXISTS pic_2024_defendant_offence_bk;

-- Create backups for this ticket

CREATE TABLE PIC_2128_court_case_bk as TABLE court_case;
CREATE TABLE PIC_2128_hearing_day_bk as TABLE hearing_day;
CREATE TABLE PIC_2128_hearing_defendant_bk as TABLE hearing_defendant;
CREATE TABLE PIC_2128_offence_bk as TABLE offence;

-- Update foreign key constraints to correctly cascade deletions

ALTER TABLE HEARING DROP CONSTRAINT fk_hearing_court_case;
ALTER TABLE HEARING ADD CONSTRAINT fk_hearing_court_case FOREIGN KEY(fk_court_case_id) REFERENCES COURT_CASE(id) ON DELETE CASCADE;

ALTER TABLE HEARING_DAY DROP CONSTRAINT fk_hearing_day_court_case;
ALTER TABLE HEARING_DAY ADD CONSTRAINT fk_hearing_day_court_case FOREIGN KEY (fk_hearing_id) REFERENCES hearing(id) ON DELETE CASCADE;

ALTER TABLE hearing_defendant DROP CONSTRAINT fk_hearing_defendant_hearing;
ALTER TABLE hearing_defendant ADD CONSTRAINT fk_hearing_defendant_hearing FOREIGN KEY (fk_hearing_id) REFERENCES hearing(id) ON DELETE CASCADE;

ALTER TABLE offence DROP CONSTRAINT fk_offence_hearing_defendant;
ALTER TABLE offence ADD CONSTRAINT fk_offence_hearing_defendant FOREIGN KEY (fk_hearing_defendant_id) REFERENCES hearing_defendant(id) ON DELETE CASCADE;

-- Clear out duplicate COMMON_PLATFORM hearings

DELETE FROM HEARING WHERE hearing_id in (SELECT case_id from court_case WHERE source_type = 'COMMON_PLATFORM');

COMMIT;
