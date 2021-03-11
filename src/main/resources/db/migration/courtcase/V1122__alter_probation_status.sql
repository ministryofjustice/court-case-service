BEGIN;
ALTER TABLE court_case ALTER COLUMN probation_status DROP NOT NULL;
update court_case set probation_status = 'CURRENT' where LOWER(probation_status) = 'current';
update court_case set probation_status = 'NO_RECORD' where LOWER(probation_status) = 'no record';
update court_case set probation_status = 'PREVIOUSLY_KNOWN' where LOWER(probation_status) = 'previously known';
update court_case set probation_status = null where lower(probation_status) like 'possible%';
COMMIT;

