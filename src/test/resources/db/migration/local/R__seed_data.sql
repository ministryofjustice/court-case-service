-- Note that this script creates hard-coded numbers for ID values would be derived from sequences when using the service.
-- The script has been changed to use negative numbers for the fields which should come from sequences.
-- This means that you can start updating cases and defendants (using sequences) without worrying about that

--INSERT INTO court (id, name, court_code) VALUES (1142408, 'North Shields', 'B10JQ');
--INSERT INTO court (id, name, court_code) VALUES (1142409, 'Sheffield Magistrates'' Court', 'B14LO');

-- LIBRA case with one hearing and one unlinked defendant
INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-1, '1a0928fb-55ad-416f-9349-669ccd4c2f1b', 1600028912, 'LIBRA', 'NEW123456');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-1, -1, '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1, -1, 'B14LO', 1,  CURRENT_DATE + INTERVAL '1 day', '09:00:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth)
VALUES (-1, '1f3895f1-8d40-4c5e-b26f-58525d488268', '1f3895f1-8d40-4c5e-b26f-58525d488267', 'Mr U UNLINKED', '{"title": "Mr", "surname": "UNLINKED", "forename1": "U"}', 'PERSON', 'MALE', '1966-03-03');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-1, -1, '1f3895f1-8d40-4c5e-b26f-58525d488267', -1);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1, -1, 'UNLINKED: Theft from a shop', 'UNLINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- LIBRA case with one hearing and two defendants - one linked and one unliked.
INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-2, '1c6bdbfc-5889-4011-94f6-89eb32b2a0b7', 1600028974, 'LIBRA', 'URN123456');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-2, -2, '1st');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-2, -2, 'B14LO', 2, CURRENT_DATE + INTERVAL '1 day', '09:00:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth, crn)
VALUES (-2, '543b3d56-02ef-4ea8-8303-182d610699b2', '543b3d56-02ef-4ea8-8303-182d610699b1', 'Mr Single LINKED', '{"title": "Mr", "surname": "LINKED", "forename1": "Single"}', 'PERSON', 'MALE', '1969-08-26', 'CRN-ALL-FIELDS');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-2, -2, '543b3d56-02ef-4ea8-8303-182d610699b1', -2);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-2, -2, 'SINGLE-LINKED: Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, date_of_birth, crn)
VALUES (-3, 'e4d515f0-e0a0-4ea8-8dda-a9de7c5f1a4e', 'e4d51e0a05f0-4ea8-8dda-a9de7c5f1a4d', 'Mr Single UNLINKED', '{"title": "Mr", "surname": "UNLINKED", "forename1": "Single"}', 'PERSON', 'MALE', '1969-08-26', null);
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-3, -2, 'e4d51e0a05f0-4ea8-8dda-a9de7c5f1a4d', -2);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3, -2, 'SINGLE-UNLINKED: Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Two COMMON_PLATFORM cases follow, each with one hearing and one defendant, both defendants have the same CRN
INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-5, 'e45b926b-885c-4137-902c-d7dbbfa4cbc0', 1600028956, 'COMMON_PLATFORM', 'URN654321');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-5, -5, '1st');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-5, -5, 'B14LO', 1, CURRENT_DATE + INTERVAL '1 day', '09:00:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, crn)
VALUES (-5, 'd9532d9f-a7c5-446e-8ef8-fdb8c7bba3c7', 'd9532d9f-a7c5-446e-8ef8-fdb8c7bba3c6', 'Mr Joe BLOGGS', '{"title": "Mr", "surname": "BLOGGS", "forename1": "Joe"}', 'PERSON', 'MALE', 'CRN-MULTI-ASSOCIATIONS');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-5, -5, 'd9532d9f-a7c5-446e-8ef8-fdb8c7bba3c6', -5);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-5, -5, 'Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-6, '40f20b13-a45e-4341-b9b3-e4754479f029', 1600028920, 'COMMON_PLATFORM', 'URN654321');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-6, -6, '1st');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-6, -6, 'B14LO', '1', CURRENT_DATE + INTERVAL '1 day', '09:30:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, crn)
VALUES (-6, 'd5f73ae8-bf95-447b-a5fe-e8174fe2bf49', 'd5f73ae8-bf95-447b-a5fe-e8174fe2bf48', 'Mr Ollie TEST', '{"title": "Mr", "surname": "TEST", "forename1": "Ollie"}', 'PERSON', 'MALE', 'CRN-MULTI-ASSOCIATIONS');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-6, -6, 'd5f73ae8-bf95-447b-a5fe-e8174fe2bf48', -6);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-6, -6, 'Theft from a shop', 'SINGLE-LINKED: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- A LIBRA case which is in the past.
INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-7, '7a794316-0b77-4ac0-8b10-3a1470d9f7a4', 1600029021, 'LIBRA','URN123456');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-7, -7, '4th');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-7, -7, 'B14LO', '1', CURRENT_DATE - INTERVAL '60 day', '09:00:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, crn)
VALUES (-7, 'e472b33c-f976-41f6-8785-70f306331c67', 'e472b33c-f976-41f6-8785-70f306331c66', 'Mrs Pauline HISTORIC', '{"title": "Mrs", "surname": "HISTORIC", "forename1": "Pauline"}', 'PERSON', 'FEMALE', 'X320741');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-7, -7, 'e472b33c-f976-41f6-8785-70f306331c66', -7);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-7, -7, 'Theft from a shop', 'HISTORIC: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- A COMMON_PLATFORM case which is next week but has some matches
INSERT INTO court_case (id, case_id, source_type, urn)
VALUES (-8, '930348c2-e7bf-489e-9d87-f9559b409c43', 'COMMON_PLATFORM', 'URN123456');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-8, -8, '2nd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-8, -8, 'B14LO', '1', CURRENT_DATE - INTERVAL '60 day', '13:00:00');
INSERT INTO DEFENDANT (id, person_id, defendant_id, defendant_name, name, type, sex, crn, pnc)
VALUES (-8, '602a9632-f5e9-42e4-8fa4-6e7c0360c4f8', '602a9632-f5e9-42e4-8fa4-6e7c0360c4f7', 'Miss Martha MATCHES', '{"title": "Miss", "surname": "MATCHES", "forename1": "Martha"}', 'PERSON', 'FEMALE', null, 'A/1332EP');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-8, -8, '602a9632-f5e9-42e4-8fa4-6e7c0360c4f7', -8);
INSERT INTO OFFENCE (ID, fk_hearing_defendant_id, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-8, -8, 'Theft from a shop', 'MATCHES: On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Matches - associated to the historic case
INSERT INTO offender_match_group (id, case_id, defendant_id, created, last_updated, created_by, last_updated_by, deleted, "version")
VALUES( -1, '930348c2-e7bf-489e-9d87-f9559b409c43', '602a9632-f5e9-42e4-8fa4-6e7c0360c4f7' , now(), now(), 'R_seed_data', '', false, 0);

INSERT INTO offender_match (id, group_id, confirmed, crn, match_type, pnc, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( -1, -1, false, 'X320741', 'NAME_DOB', 'PNC1', now(), now(), 'R_seed_data', '', false, 0, false);
INSERT INTO offender_match (id, group_id, confirmed, crn, match_type, pnc, created, last_updated, created_by, last_updated_by, deleted, "version", rejected)
VALUES( -2, -1, false, 'X320811', 'NAME_DOB', 'PNC2', now(), now(), 'R_seed_data', '', false, 0, false);

-- A LIBRA case which has one defendant, matched
INSERT INTO court_case (id, case_id, case_no, source_type, urn)
VALUES (-9, 'd0d6d6db-1b52-49d3-bd8a-5f290c2621eb', '9600000000', 'LIBRA', 'NEW123456');
INSERT INTO HEARING (id, fk_court_case_id, list_no)
VALUES (-9, -9, '1st');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-9, -9, 'B14LO', '1', CURRENT_DATE + INTERVAL '2 day', '09:00:00');
INSERT INTO DEFENDANT (id, person_id, DEFENDANT_ID, defendant_name, name, type, sex, crn)
VALUES (-9, '9f451171-92f7-4c48-a831-ffc630709810', '9f451171-92f7-4c48-a831-ffc630709819', 'Mrs Josephine BAKER', '{"title": "Mrs", "surname": "BAKER", "forename1": "Josephine"}', 'PERSON', 'FEMALE', 'X630852');
INSERT INTO hearing_defendant (id, fk_hearing_id, defendant_id, fk_defendant_id)
VALUES (-9, -9, '9f451171-92f7-4c48-a831-ffc630709819', -9);
