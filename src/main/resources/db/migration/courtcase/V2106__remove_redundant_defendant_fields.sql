BEGIN;

alter table defendant drop column previously_known_termination_date;
alter table defendant drop column suspended_sentence_order;
alter table defendant drop column breach;
alter table defendant drop column pre_sentence_activity;
alter table defendant drop column awaiting_psr;
alter table defendant drop column probation_status;

COMMIT;
