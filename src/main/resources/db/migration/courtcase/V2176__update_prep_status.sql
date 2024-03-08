BEGIN;

alter table hearing_defendant add column prep_status TEXT NOT NULL default 'NOT_STARTED';

alter table if exists hearing_defendant_aud add column prep_status TEXT NOT NULL default 'NOT_STARTED';

END;