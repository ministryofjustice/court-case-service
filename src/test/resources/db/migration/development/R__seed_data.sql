-- Note that this script creates hard-coded numbers for ID values would be derived from sequences when using the service.
-- The script has been changed to use negative numbers for the fields which should come from sequences.
-- This means that you can start updating cases and defendants (using sequences) without worrying about that
-- TODO - at the time of writing I have to populate defendant_name, session_start_time and court_room but these should now be in other tables

INSERT INTO court (id, name, court_code) VALUES (1142408, 'North Shields', 'B10JQ');
INSERT INTO court (id, name, court_code) VALUES (1142409, 'Sheffield Magistrates'' Court', 'B14LO');

-- LIBRA case with one hearing and one unlinked defendant
INSERT INTO court_case (id, case_id, case_no, source_type, session_start_time, defendant_name, court_room)
VALUES (-1, '1a0928fb-55ad-416f-9349-669ccd4c2f1b', 1600028912, 'LIBRA', NOW() + INTERVAL '1 day', 'Mr U UNLINKED', '1');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1, -1, 'B14LO', 1,  CURRENT_DATE + INTERVAL '1 day', '09:00:00', '3rd');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth)
VALUES (-1, -1, '1f3895f1-8d40-4c5e-b26f-58525d488267', 'Mr U UNLINKED', '{"title": "Mr", "surname": "U", "forename1": "UNLINKED"}', 'PERSON', 'M', '1966-03-03');
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1, -1, 'UNLINKED: Theft from a shop', 'UNLINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- LIBRA case with one hearing and two defendants - one linked and one unliked.
INSERT INTO court_case (id, case_id, case_no, source_type, session_start_time, defendant_name, court_room)
VALUES (-2, '1c6bdbfc-5889-4011-94f6-89eb32b2a0b7', 1600028974, 'LIBRA', NOW() + INTERVAL '1 day', 'Mr Single UNLINKED', '2');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-2, -2, 'B14LO', 2, CURRENT_DATE + INTERVAL '1 day', '09:00:00', '1st');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr)
VALUES (-2, -2, '543b3d56-02ef-4ea8-8303-182d610699b1', 'Mr Single LINKED', '{"title": "Mr", "surname": "Single", "forename1": "LINKED"}', 'PERSON', 'M', '1969-08-26', 'CRN-ALL-FIELDS', 'CURRENT', '2019-08-19', true, true, true, true);
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-2, -2, 'SINGLE-LINKED: Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth, crn)
VALUES (-3, -2, 'e4d515f0-e0a0-4ea8-8dda-a9de7c5f1a4d', 'Mr Single UNLINKED', '{"title": "Mr", "surname": "Single", "forename1": "UNLINKED"}', 'PERSON', 'M', '1969-08-26', null);
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3, -2, 'SINGLE-UNLINKED: Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Two COMMON_PLATFORM cases follow, each with one hearing and one defendant, both defendants have the same CRN
INSERT INTO court_case (id, case_id, case_no, source_type, session_start_time, defendant_name, court_room)
VALUES (-5, 'e45b926b-885c-4137-902c-d7dbbfa4cbc0', 1600028956, 'COMMON_PLATFORM', NOW() + INTERVAL '1 day', 'Mr J BLOGGS', '1');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-5, -5, 'B14LO', '1', CURRENT_DATE + INTERVAL '1 day', '09:00:00', '1st');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr)
VALUES (-5, -5, 'd9532d9f-a7c5-446e-8ef8-fdb8c7bba3c6', 'Mr Joe BLOGGS', '{"title": "Mr", "surname": "Joe", "forename1": "BLOGGS"}', 'PERSON', 'M', 'CRN-MULTI-ASSOCIATIONS', null, '2019-08-19', null, false, true, false);
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-5, -5, 'Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

INSERT INTO court_case (id, case_id, case_no, source_type, session_start_time, defendant_name, court_room)
VALUES (-6, '40f20b13-a45e-4341-b9b3-e4754479f029', 1600028920, 'COMMON_PLATFORM', NOW() + INTERVAL '1 day', 'Mr Ollie TEST', '1');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-6, -6, 'B14LO', '1', CURRENT_DATE + INTERVAL '1 day', '09:30:00', '1st');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr)
VALUES (-6, -6, 'd5f73ae8-bf95-447b-a5fe-e8174fe2bf48', 'Mr Ollie TEST', '{"title": "Mr", "surname": "Ollie", "forename1": "TEST"}', 'PERSON', 'M', 'CRN-MULTI-ASSOCIATIONS', null, '2019-08-19', null, false, true, false);
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-6, -6, 'Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- A LIBRA case which is in the past.
INSERT INTO court_case (id, case_id, case_no, source_type, session_start_time, defendant_name, court_room)
VALUES (-7, '7a794316-0b77-4ac0-8b10-3a1470d9f7a4', 1600029021, 'LIBRA', NOW() - INTERVAL '60 day', 'Mrs Pauline HISTORIC', '1');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-7, -7, 'B14LO', '1', CURRENT_DATE - INTERVAL '60 day', '09:00:00', '4th');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr)
VALUES (-7, -7, 'e472b33c-f976-41f6-8785-70f306331c66', 'Mrs Pauline HISTORIC', '{"title": "Mrs", "surname": "Pauline", "forename1": "HISTORIC"}', 'PERSON', 'F', 'X320741', 'CURRENT', '2019-08-19', null, false, true, false);
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-7, -7, 'Theft from a shop', 'HISTORIC: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- A COMMON_PLATFORM case which is next week but has some matches
INSERT INTO court_case (id, case_id, source_type, session_start_time, defendant_name, court_room)
VALUES (-8, '930348c2-e7bf-489e-9d87-f9559b409c43', 'COMMON_PLATFORM', NOW() + INTERVAL '7 day', 'Miss Martha MATCHES', '1');
INSERT INTO HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-8, -8, 'B14LO', '1', CURRENT_DATE - INTERVAL '60 day', '13:00:00', '2nd');
INSERT INTO DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, sex, crn, pnc)
VALUES (-8, -8, '602a9632-f5e9-42e4-8fa4-6e7c0360c4f7', 'Miss Martha MATCHES', '{"title": "Miss", "surname": "Martha", "forename1": "MATCHES"}', 'PERSON', 'F', null, 'A/1332EP');
INSERT INTO DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-8, -8, 'Theft from a shop', 'MATCHES: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Matches - associated to the historic case
INSERT INTO offender_match_group (id, case_id, defendant_id, created, last_updated, created_by, last_updated_by, deleted, "version")
VALUES( -1, '930348c2-e7bf-489e-9d87-f9559b409c43', '602a9632-f5e9-42e4-8fa4-6e7c0360c4f7' , now(), now(), 'R_seed_data', '', false, 0);

INSERT INTO offender_match (id, group_id, confirmed, crn, match_type, pnc, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( -1, -1, false, 'X320741', 'NAME_DOB', 'PNC1', now(), now(), 'R_seed_data', '', false, 0, false);
INSERT INTO offender_match (id, group_id, confirmed, crn, match_type, pnc, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( -2, -1, false, 'X320811', 'NAME_DOB', 'PNC2', now(), now(), 'R_seed_data', '', false, 0, false);




