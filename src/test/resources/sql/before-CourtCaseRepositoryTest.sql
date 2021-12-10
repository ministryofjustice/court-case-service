
-- START DEFINITION OF CASE_ID created_clash_id_1
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, defendant_name, name, defendant_address, pnc, cro, defendant_dob, defendant_sex, nationality_1, nationality_2, created, source_type)
VALUES (-1700028900, 'created_clash_id_1', 1600028913, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'A/1234560BA', '311462/13E', '1958-10-10', 'MALE', 'British', 'Polish' , '2020-09-01 16:59:59.000', 'LIBRA');

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER, CREATED)
VALUES (-1700028900, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER, CREATED)
VALUES (-1700028900, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2, '2020-09-01 16:59:59.000');

INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00', '3rd', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, awaiting_psr, breach, pre_sentence_activity, suspended_sentence_order, previously_known_termination_date, created)
VALUES (-1000000, -1700028900, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', true, true, true, true, '2010-01-01', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2, '2020-09-01 16:59:59.000');


-- SECOND RECORD WITH SAME CREATED TIMESTAMP
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, defendant_name, name, defendant_address, pnc, cro, defendant_dob, defendant_sex, nationality_1, nationality_2, created, source_type)
VALUES (-1700028901, 'created_clash_id_1', 1600028913, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'A/1234560BA', '311462/13E', '1958-10-10', 'MALE', 'British', 'Polish' , '2020-09-01 16:59:59.000', 'LIBRA');

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028901, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028901, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1000001, -1700028901, 'B10JQ', 1, '2019-12-14', '09:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, awaiting_psr, breach, pre_sentence_activity, suspended_sentence_order, previously_known_termination_date)
VALUES (-1000001, -1700028901, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', true, true, true, true, '2010-01-01');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000002, -1000001, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000003, -1000001, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

-- END DEFINITION OF CASE_ID created_clash_id_1
