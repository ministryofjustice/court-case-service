INSERT INTO court (id, name, court_code) VALUES (1142407, 'Sheffield Magistrates Court', 'SHF');
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (1168460, 1600028912, 'SHF', 1, '2019-12-14 09:00', 'No record', '2019-12-14 09:00', null, false, false, 'Mr Julian Cone', '{"line1": "77", "line2":"Castle Crescent", "postcode":"DN1 2SD","line3":"Doncaster", "line4": "South Yorkshire"}', 'X320741', 'A/1234560BA', '18763/79J', '3rd', '1958-10-31', 'M', 'British', null);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (1246257, 1600028974, 'SHF', 1, '2020-01-13 09:00', 'Current', '2019-12-14 09:00', null, true, true, 'Mr Dylan Adam Armstrong', '{"line1": "27", "line2":"Elm Place", "postcode":"ad21 5dr","line3":"Bangor"}', 'X320741', 'A/1234560BA', '311462/13E', '1st', '1977-12-11', 'M', 'British', null);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (1246258, 1600028956, 'SHF', 1, '2020-01-13 09:00', 'Previously known', '2000-01-01','2019-12-14 09:00', true, true, 'Mr Joe Bloggs', '{"line1": "103", "line2":"Warwick Avenue", "postcode":"S1 6UA","line3":"Sheffield"}', 'X320741', 'D/1234560BA', '311499/16F', '1st', '1983-02-02', 'M', 'Polish', null);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (1246273, 1600028920, 'SHF', 1, '2020-01-13 09:00', 'No record', '2019-12-14 09:00', null, false, false, 'Mr Ollie Test', '{"line1": "65", "line2":"Relish Avenue", "postcode":"LS3 5FA","line3":"Leeds", "line4": "West Yorkshire"}', 'X320741', 'B/1234560BB', '317462/19X', '2nd', '1996-12-03', 'M', 'British', null);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (1248278, 1600029021, 'SHF', 1, '2020-01-13 09:00', 'No record', '2019-12-14 09:00', null, false, false, 'Mr Ureet JMBALERNAEU', '{"line1": "56", "line2":"Henderson Walk", "postcode":"S2 6GA","line3":"Sheffield"}', 'X320741', 'D/2134650CA', '766412/20R', '3rd', '1966-03-03', 'M', 'British', null);


INSERT INTO OFFENCE (
	CASE_NO,
	COURT_CODE,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1600028912,
        'SHF',
        'Theft from a shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1
	);

INSERT INTO OFFENCE (
	CASE_NO,
	COURT_CODE,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1600028912,
        'SHF',
        'Theft from a different shop',
        'On 02/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        2
	);

INSERT INTO offender_match_group
( case_no, court_code, created, last_updated, created_by, last_updated_by, deleted, "version")
VALUES( '1600029021', 'SHF', now(), now(), 'R_seed_data', '', false, 0);

INSERT INTO offender_match
(confirmed, crn, cro, match_type, pnc, group_id, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( false, 'X320741', null, 'NAME_DOB', '', 1, now(), now(), 'R_seed_data', '', false, 0, false);

INSERT INTO offender_match
(confirmed, crn, cro, match_type, pnc, group_id, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( false, 'X320811', null, 'NAME_DOB', '', 1, now(), now(), 'R_seed_data', '', false, 0, false);




