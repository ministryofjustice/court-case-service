-- START DEFINITION OF CASE_ID created_clash_id_1
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028900, 'created_clash_id_1', '1600028913', '2020-09-01 16:59:59.000', 'LIBRA');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-1700028900, -1700028900, 'created_clash_id_1', '2020-09-01 16:59:59.000', '3rd');

INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, created)
VALUES (-1000000, '40db17d6-04db-11ec-b2d8-0242ac130002', 'da3ee1c2-4ba8-11ed-bdc3-0242ac120002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-1000000, -1700028900, '2020-09-01 16:59:59.000', '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2, '2020-09-01 16:59:59.000');


-- SECOND RECORD WITH SAME CREATED TIMESTAMP
/*INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028901, 'created_clash_id_1', '1600028913', '2020-09-01 16:59:59.000', 'LIBRA');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028901, -1700028901, 'created_clash_id_1', '2020-09-01 16:59:59.000');

INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000001, -1700028901, 'B10JQ', 1, '2019-12-14', '09:00', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, defendant_id)
VALUES (-1000001, -1700028901, '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000002, -1000001, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000003, -1000001, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);
*/
-- END DEFINITION OF CASE_ID created_clash_id_1

-- START DEFINITION OF CASES FOR TESTING CASE LIST

-- 1 Basic case Libra
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028952, 'case_list_1', 1600028914, '2020-09-01 16:59:59.000', 'LIBRA');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028952, -1700028952, 'case_list_1', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000054, -1700028952, 'B10JQ', 1, '2022-02-17', '09:00', '2020-09-01 16:59:59.000');

-- 2 Basic case Common Platform
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028953, 'case_list_2', 1600028914, '2020-09-01 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028953, -1700028953, 'case_list_2', '2020-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000055, -1700028953, 'B10JQ', 1, '2022-02-17', '09:00', '2020-09-01 16:59:59.000');

-- 3 Moved to this court
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028904, 'case_list_3', 1600028914, '2022-09-01 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028904, -1700028904, 'case_list_3', '2022-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000006, -1700028904, 'B10JQ', 1, '2022-02-17', '09:00', '2022-09-01 16:59:59.000');

-- 4 Moved out of this court
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028906, 'case_list_4', 1600028914, '2022-09-01 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028906, -1700028906, 'case_list_4', '2022-09-01 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1000008, -1700028906, 'B14LO', 1, '2022-02-17', '09:00', '2022-09-01 16:59:59.000');

-- END DEFINITION OF CASES FOR TESTING CASE LIST
