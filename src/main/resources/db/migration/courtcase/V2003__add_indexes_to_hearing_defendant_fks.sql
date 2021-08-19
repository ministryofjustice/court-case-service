create index court_case_case_id_idx on court_case (case_id);
create index hearing_court_case_id_idx on hearing (court_case_id);
create index defendant_court_case_id_idx on defendant (court_case_id);
create index defendant_offence_defendant_id_idx on defendant_offence (defendant_id);
