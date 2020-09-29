BEGIN;
update court_case set probation_status = 'Previously known' where probation_status = 'Previously Known';
COMMIT;
