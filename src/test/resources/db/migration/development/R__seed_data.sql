-- Note that this script creates hard-coded ID numbers for ID values which ought to come from sequences.
-- If you need to start inserting data via the app after creating the seed data, you will get conflicts so may need to reset the sequence with something like
-- ALTER SEQUENCE court_case_id_seq INCREMENT BY 1 MINVALUE 10 RESTART WITH 10 start with 10

INSERT INTO court (id, name, court_code) VALUES (1142407, 'Sheffield Magistrates Court', 'SHF');
INSERT INTO court (id, name, court_code) VALUES (1142408, 'North Shields', 'B10JQ00');
INSERT INTO court (id, name, court_code) VALUES (1142409, 'Sheffield Magistrates'' Court', 'B14LO00');

INSERT INTO court_case
    (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, source_type)
VALUES
    (-1, 1168460, 1600028912, 'SHF', 1, '2019-12-14 09:00', 'No record', null, false, false, 'Mr Julian Cone', '{"line1": "77", "line2":"Castle Crescent", "postcode":"DN1 2SD","line3":"Doncaster", "line4": "South Yorkshire"}', 'X320741', 'A/1234560BA', '18763/79J', '3rd', '1958-10-31', 'M', 'British', null, 'LIBRA');

INSERT INTO court_case
    (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, source_type)
VALUES
    (-2, 1246257, 1600028974, 'SHF', 1, '2020-01-13 09:00', 'Current', null, true, true, 'Mr Dylan Adam Armstrong', '{"line1": "27", "line2":"Elm Place", "postcode":"ad21 5dr","line3":"Bangor"}', 'X320741', 'A/1234560BA', '311462/13E', '1st', '1977-12-11', 'M', 'British', null, 'LIBRA');

INSERT INTO court_case
(id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, source_type)
VALUES
    (-3, 1246258, 1600028956, 'SHF', 1, '2020-01-13 09:00', 'Previously known', '2019-12-14 09:00', true, true, 'Mr Joe Bloggs', '{"line1": "103", "line2":"Warwick Avenue", "postcode":"S1 6UA","line3":"Sheffield"}', 'X320741', 'D/1234560BA', '311499/16F', '1st', '1983-02-02', 'M', 'Polish', null, 'LIBRA');

INSERT INTO court_case
    (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, source_type)
VALUES
    (-4, 1246273, 1600028920, 'SHF', 1, '2020-01-13 09:00', 'No record', null, false, false, 'Mr Ollie Test', '{"line1": "65", "line2":"Relish Avenue", "postcode":"LS3 5FA","line3":"Leeds", "line4": "West Yorkshire"}', 'X320741', 'B/1234560BB', '317462/19X', '2nd', '1996-12-03', 'M', 'British', null, 'LIBRA');

INSERT INTO court_case
    (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, source_type)
VALUES
    (-5, '8f1f74be-7c63-4226-abbe-a4292d3d2592', 1600029021, 'SHF', 1, '2020-01-13 09:00', 'No record', null, false, false, 'Mr Ureet JMBALERNAEU', '{"line1": "56", "line2":"Henderson Walk", "postcode":"S2 6GA","line3":"Sheffield"}', 'X320741', 'D/2134650CA', '766412/20R', '3rd', '1966-03-03', 'M', 'British', null, 'COMMON_PLATFORM');

INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, pnc, cro, type, sex, probation_status, date_of_birth)
VALUES (-2, -5, 'e10762d4-b874-43e1-9b51-0997b888d134', 'Mr Ureet JMBALERNAEU', '{"title": "Mr", "surname": "JMBALERNAEU", "forename1": "Ureet"}', 'X320741', 'D/2134650CA', '999999/20R', 'PERSON', 'M', 'NO_RECORD', '1966-03-03');

INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, pnc, cro, type, sex, probation_status, date_of_birth)
VALUES (-1, -5, '183b84fb-7142-4f41-8a9b-6f3c9905df97', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'E/2134650CA', '888888/20R', 'PERSON', 'M', 'NO_RECORD', '1969-03-03');


INSERT INTO offender_match_group
( id, case_id, defendant_id, created, last_updated, created_by, last_updated_by, deleted, "version")
VALUES( 1, '1248278', 'e10762d4-b874-43e1-9b51-0997b888d134' , now(), now(), 'R_seed_data', '', false, 0);

INSERT INTO offender_match
(confirmed, crn, cro, match_type, pnc, group_id, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( false, 'X320741', null, 'NAME_DOB', '', 1, now(), now(), 'R_seed_data', '', false, 0, false);

INSERT INTO offender_match
(confirmed, crn, cro, match_type, pnc, group_id, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( false, 'X320811', null, 'NAME_DOB', '', 1, now(), now(), 'R_seed_data', '', false, 0, false);




