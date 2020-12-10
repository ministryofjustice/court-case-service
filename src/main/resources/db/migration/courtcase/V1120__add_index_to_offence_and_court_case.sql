create index idx_offence_court_case_idx on offence (court_case_id);
create index idx_court_case_court_code_case_no on court_case (court_code, case_no);
