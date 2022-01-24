BEGIN;

alter table COURT_CASE drop column PROBATION_STATUS;
alter table COURT_CASE drop column PREVIOUSLY_KNOWN_TERMINATION_DATE;
alter table COURT_CASE drop column SUSPENDED_SENTENCE_ORDER;
alter table COURT_CASE drop column BREACH;
alter table COURT_CASE drop column PRE_SENTENCE_ACTIVITY;
alter table COURT_CASE drop column DEFENDANT_NAME;
alter table COURT_CASE drop column NAME;
alter table COURT_CASE drop column DEFENDANT_ADDRESS;
alter table COURT_CASE drop column DEFENDANT_DOB;
alter table COURT_CASE drop column DEFENDANT_SEX;
alter table COURT_CASE drop column DEFENDANT_TYPE;
alter table COURT_CASE drop column CRN;
alter table COURT_CASE drop column PNC;
alter table COURT_CASE drop column CRO;
alter table COURT_CASE drop column NATIONALITY_1;
alter table COURT_CASE drop column NATIONALITY_2;
alter table COURT_CASE drop column AWAITING_PSR;

COMMIT;