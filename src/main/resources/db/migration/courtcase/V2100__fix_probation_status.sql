BEGIN;
-- Similar SQL has been applied before in V1122__alter_probation_status.sql
-- Need to do this on defendant now. Bug fix in this changeset which should transform incoming "No record" and such to NO_RECORD from now on
update court_case set probation_status = 'CURRENT' where LOWER(probation_status) = 'current';
update court_case set probation_status = 'NO_RECORD' where LOWER(probation_status) = 'no record';
update court_case set probation_status = 'PREVIOUSLY_KNOWN' where LOWER(probation_status) = 'previously known';

update defendant set probation_status = 'CURRENT' where LOWER(probation_status) = 'current';
update defendant set probation_status = 'NO_RECORD' where LOWER(probation_status) = 'no record';
update defendant set probation_status = 'PREVIOUSLY_KNOWN' where LOWER(probation_status) = 'previously known';
COMMIT;
