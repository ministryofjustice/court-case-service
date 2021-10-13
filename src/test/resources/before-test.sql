TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.offence CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;

INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('North Shields', 'B10JQ');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Sheffield', 'B14LO');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Leicester', 'B33HU');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Aberystwyth', 'B63AD');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('New New York', 'B30NY');

-- START DEFINITION OF CASE NO 1700028913
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, awaiting_psr, source_type) VALUES (-1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 1600028913, 'B10JQ', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', null, 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now(), true, 'LIBRA');

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028900, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028900, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr)
VALUES (-1000000, -1700028900, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'M', 'British', 'Polish', null, '2010-01-01', true, true, true, true);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);
-- END DEFINITION OF 1700028913

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type, list_no)
VALUES (-1700029901, '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', 1600028914, 'B10JQ', 1, '2019-12-14 07:00', 'NOT_SENTENCED', 'X320742','Mr Billy ZANE', now(), 'COMMON_PLATFORM', '3rd');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700029901, -1700029901, 'B10JQ', 1, '2019-12-14', '07:00', '3rd');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700029902, -1700029901, 'B10JQ', 2, '2019-12-15', '13:00', '4th');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-1700029902, -1700029901, '7a319a46-037c-481c-ab1e-dbfab62af4d6', 'Mr Billy ZANE', '{"title": "Mr", "surname": "ZANE", "forename1": "Billy"}', '{"line1": "Billy Place", "line2": "Zane Pla", "postcode": "z12 8gt 5dr"}', 'PERSON', '1987-10-10', 'A/1234560BA', '311462/13E', 'M', 'British', 'Polish');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1700029902, -1700029902, 'Billy stole from a shop', 'On 01/01/2015 at own, Billy stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2, PROBATION_STATUS)
VALUES (-1700029903, -1700029901, '7a320a46-037c-481c-ab1e-dbfab62af4d6', 'Ms Emma Radical', '{"title": "Ms", "surname": "RADICAL", "forename1": "Emma"}', '{"line1": "Emma Place", "line2": "Radical Place", "postcode": "e12 8gt"}', 'PERSON', '1987-10-10', 'A/1234560CD', '311465/13F', 'F', 'Romanian', 'Chinese', 'CURRENT');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1700029903, -1700029903, 'Emma stole 1st thing from a shop', 'On 01/01/2015 at own, Emma stole 1st article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1700029904, -1700029903, 'Emma stole 2nd thing from a shop', 'On 01/01/2015 at own, Emma stole 2nd article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, source_type)
VALUES (-1700028902, '1f93aa0a-7e46-4885-a1cb-f25a4be33a57', 1600028915, 'B10JQ', 1, '2019-12-14 23:59:59', now(), 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028902, -1700028902, 'B10JQ', 1, '2019-12-14', '23:59:59', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028902, -1700028902, 'f15bd32c-119d-447e-bbb6-02b56c36f133', 'Mr Nicholas CAGE', '{"title": "Mr", "surname": "CAGE", "forename1": "Nicholas"}', 'X320743', 'PERSON', 'M', 'NO_RECORD');

-- See CourtCaseControllerIntTest.GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime()
-- These records are used to test the createdAfter filters
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, source_type)
VALUES (-1700028903, '1f93aa0a-7e46-4885-a1cb-f25a4be33a58', 1600028916, 'B10JQ', 1, '2019-12-14 12:59:59', '2020-09-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028903, -1700028903, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028903, -1700028903, '44817de0-cc89-460a-8f07-0b06ef45982a', 'Mr Mads MIKKELSEN', '{"title": "Mr", "surname": "MIKKELSEN", "forename1": "Mads"}', 'X320744', 'PERSON', 'M', 'NO_RECORD');

-- 2 versions for case id '1f93aa0a-7e46-4885-a1cb-f25a4be33a58'
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028904, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', 'CURRENT', 'X320745', 'Mr Hideo Kojima', '2020-10-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028904, -1700028904, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, source_type)
VALUES (-1700028905, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', '2020-10-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028905, -1700028905, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028905, -1700028905, '965c0391-8929-4fff-b88c-2f813cf16d43', 'Mr Hideo KOJIMA', '{"title": "Mr", "surname": "KOJIMA", "forename1": "Hideo"}', 'X320745', 'PERSON', 'M', 'NO_RECORD');

-- See GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate
-- Used to test default createdBefore date
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, source_type)
VALUES (-1700028906, '1f93aa0a-7e46-4885-a1cb-f25a4be33a30', 1600028930, 'B10JQ', 1,'2020-05-01 12:59:59', '2020-05-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028906, -1700028906, 'B10JQ', 1, '2020-05-01', '12:59:59', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028906, -1700028906, '965c0391-8929-4fff-b88c-2f813cf16d43', 'Mr Hideo KOJIMA', '{"title": "Mr", "surname": "KOJIMA", "forename1": "Hideo"}', 'X320745', 'PERSON', 'M', 'NO_RECORD');

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test that the createdToday field returns false if a new record was created today updating an existing one
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028907, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', 'NO_RECORD', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028907, -1700028907, 'B10JQ', 2, '2019-12-14', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, source_type)
VALUES (-1700028908, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', now(), 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028908, -1700028908, 'B10JQ', 2, '2019-12-14', '13:00:00', '3rd', now());
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028908, -1700028908, '3bf70cd8-7e9d-4d29-b9b2-f8f7f898cb32', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'X320746', 'PERSON', 'M', 'NO_RECORD');

-- See GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, created_by, manual_update, source_type, crn, probation_status)
VALUES (-1700028909, 'e652eaae-1114-4593-8f56-659eb2baffcf', 1600028919, 'B30NY', 1, '2200-12-14 12:59:59', '2020-10-01 16:59:59', 'TURANGALEE(prepare-a-case-for-court)', true, 'COMMON_PLATFORM', 'X320654', 'NO_RECORD');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028909, -1700028909, 'B30NY', 1, '2200-12-14', '12:59:59', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, pnc, sex, probation_status, crn)
VALUES (-1700028909, -1700028909, 'c15475ce-9748-4a60-b42b-02ce78523c95', 'Mr Hubert FARNSWORTH', '{"title": "Mr", "surname": "FARNSWORTH", "forename1": "Hubert"}', '{"line1": "Anfield", "line2": "Walton Breck Road", "postcode": "L5 2DE 5dr"}', 'PERSON', '1940-05-01', 'A/1234560BA', 'M', 'NO_RECORD', 'X320654');

-- These records are used to test the Last-Modified header CHECKED
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted, source_type)
VALUES (-1700028910, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, 'B14LO', 2,'2021-06-01 13:00:00', 'No record', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', false, 'LIBRA');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028910, -1700028910, 'B14LO', 1, '2021-06-01', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted, source_type)
VALUES (-1700028911, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, 'B14LO', 2,'2021-06-01 13:00:00', 'No record', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028911, -1700028911, 'B14LO', 2, '2021-06-01', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, deleted, source_type)
VALUES (-1700028912, '1f93aa0a-7e46-4885-a1cb-f25a4be33a20', 1600128920, 'B14LO', 2,'2021-06-01 13:00:00', '2021-06-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028912, -1700028912, 'B14LO', 2, '2021-06-01', '13:00:00', '3rd', '2021-06-01 16:59:59');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, crn, type, sex, probation_status)
VALUES (-1700028912, -1700028912, '03137ac2-8c92-471a-aed2-c92ea6e4963e', 'Mr George O''DOWD', '{"title": "Mr", "surname": "O''DOWD", "forename1": "George"}', 'X320746', 'PERSON', 'M', 'NO_RECORD');

-- See CourtCaseControllerPutIntTest.whenPurgeCases_ThenReturn204NoContent() CHECKED
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, source_type)
VALUES (1000000, 1000000, 1000000, 'B10JQ', '1', '2100-01-01 09:00:00', 'No record', 'X320799', 'Mr Tom Cruise', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000001, 1000001, 1000001, 'B10JQ', '1', '2020-01-01 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000002, 1000002, 1000002, 'B10JQ', '3', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-1000002, 1000002, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'M', 'British', 'Polish');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000003, 1000003, 1000003, 'B10JQ', '2', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000004, 1000004, 1000004, 'B10JQ', '1', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000005, 1000005, 1000005, 'B10JQ', '3', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000006, 1000006, 1000006, 'B10JQ', '2', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000007, 1000007, 1000007, 'B10JQ', '1', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000008, 1000008, 1000008, 'B10JQ', '1', '2020-01-03 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000009, 1000009, 1000009, 'B10JQ', '1', '2020-01-03 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (1000001, 1000001, 'Title', 'Summary.', 'ACT.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (1000002, 1000002, 'Title', 'Summary.', 'ACT.', 2);


INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE, CASE_ID, DEFENDANT_ID)
VALUES (9999991, '1600028913','B10JQ', '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE, CASE_ID, DEFENDANT_ID)
VALUES (9999992, '1600028914','B10JQ', '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', '7a320a46-037c-481c-ab1e-dbfab62af4d6');


INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320741', '12345', 'NAME_DOB', '123456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, '2234', '22345', 'NAME_DOB', '223456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (true, false, 'X6666', '78654', 'NAME_DOB', '323456', 9999991);

INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, '3234', '32345', 'NAME_DOB', '323456', 9999992);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, 'CRN123', '32345', 'NAME_DOB', 'PNC12/456', 9999992);

INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE, CASE_ID, DEFENDANT_ID)
VALUES (9999993, '1000002', 'B10JQ', '1000002', '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', '323456', 9999993);

-- See CourtCaseControllerPutIntTest.whenUpdateCaseDataByCourtAndCaseNo_ThenUpdateProbationStatusOnCasesWithSameCrn()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (15000, 15000, '15000', 'B10JQ', '1', NOW() + INTERVAL '1 day', 'No record', 'X320747', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (15001, 15001, '15001', 'B10JQ', '1', NOW() + INTERVAL '2 days', 'Current', 'X320747', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (15002, 15002, '15002', 'B10JQ', '1', NOW() - INTERVAL '7 days', 'No record', 'X320747', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15000, 15000, 'Offence Title 15000', 'Offence Summary 15000', 'Offence ACT 15000', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15001, 15000, 'Offence Title 15001', 'Offence Summary 15001', 'Offence ACT 15001', 1);

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15002, 15001, 'Offence Title 15002', 'Offence Summary 15002', 'Offence ACT 15002', 1);

--
-- These records are used to generate cases / offences / matches for the PACT verification tests
-- We will have 3 case sections in the case ist but with 2 unique case IDs and 3 defendant IDs
-- Case id 1 : 683bcde4-611f-4487-9833-f68090507b74, defendants 005ae89b-46e9-4fa5-bb5e-d117011cab32 and f2c83643-8ebd-4609-9183-cd8c34984e33
-- Case id 2 : 2243231a-7810-496c-bd41-cb01ceb1fe0b, defendant bfd7df09-4177-475e-b16f-0ace34a5ef2f
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, created_by, source_type) VALUES
(16000, '683bcde4-611f-4487-9833-f68090507b74', 16000, 'B10JQ', '1', '2020-02-29 09:00', now(), 'ChrisFaulkner(manual-PIC-959)', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, created, created_by, source_type, defendant_type) VALUES
(16001, '2243231a-7810-496c-bd41-cb01ceb1fe0b', 16001, 'B10JQ', '10', '2020-02-29 14:00', now(), 'ChrisFaulkner(manual-PIC-959)', 'LIBRA', 'ORGANISATION');

-- 683bcde4-611f-4487-9833-f68090507b74 - Hearing / defendants / defendant offences for case id
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-16000, 16000, 'B10JQ', 1, '2020-02-29', '14:00:00', '1st');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, sex, nationality_1, nationality_2, probation_status, previously_known_termination_date, breach, pre_sentence_activity, suspended_sentence_order)
VALUES (-16000, 16000, '005ae89b-46e9-4fa5-bb5e-d117011cab32', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": "line4", "line5": "line5"}', 'PERSON', '1958-10-10', 'C16000', 'A/160000BA', 'M', 'British', 'Polish', 'NO_RECORD', '2010-01-01', true, true, true);
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, sex, nationality_1, probation_status)
VALUES (-16001, 16000, 'f2c83643-8ebd-4609-9183-cd8c34984e33', 'Mr Brian CANT', '{"title": "Mr", "surname": "CANT", "forename1": "Brian", "forename2": "Bryan", "forename3": "Eric"}', '{"line1": "26", "line2": "Elms Road", "postcode": "LE2 3LU", "line3": "Leicester"}', 'PERSON', '1939-10-10', 'D16000', 'D/160000BA', 'M', 'British', 'NOT_SENTENCED');

INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-16000, -16000, 'Offence Title 16000', 'Offence Summary 16000', 'Offence ACT 16000', 1);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-16001, -16001, 'Offence Title 16001', 'Offence Summary 16001', 'Offence ACT 16001', 1);

-- 2243231a-7810-496c-bd41-cb01ceb1fe0b - Hearing / defendants / defendant offences for case id
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-16001, 16001, 'B10JQ', 10, '2020-02-29', '14:00:00', '1st');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, probation_status)
VALUES (-16002, 16001, 'bfd7df09-4177-475e-b16f-0ace34a5ef2f', 'ACME MOTORS LTD', '{"surname": "ACME MOTORS LTD"}', '{"line1": "Freemans Common", "line2": "Someplace", "postcode": "XX1 1XX", "line3": "Leicester"}', 'ORGANISATION', 'NO_RECORD');

INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_ID, DEFENDANT_ID, CASE_NO, COURT_CODE)
VALUES (16000, '683bcde4-611f-4487-9833-f68090507b74','005ae89b-46e9-4fa5-bb5e-d117011cab32', 16000, 'B10JQ');

INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', 'A/160000BA', 16000);

--
-- Section to populate an entirely new COURT CASE with separate HEARING, DEFENDANT, DEFENDANT_OFFENCE. Also a caseId which looks like a UUID which is what will be happening post CP integration
INSERT INTO courtcaseservicetest.COURT_CASE (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, awaiting_psr, source_type)
VALUES (-1700030000, 'ac24a1be-939b-49a4-a524-21a3d2230000', '1700030000', 'B14LO', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', null, 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now() - interval '2 hours', true, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700030000, -1700030000, 'B14LO', 1, '2019-12-14', '09:00:00', '3rd', now() - interval '2 hours');

INSERT INTO courtcaseservicetest.COURT_CASE (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, awaiting_psr, source_type)
VALUES (-1700030001, 'ac24a1be-939b-49a4-a524-21a3d2230000', '1700030000', 'B14LO', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', null, 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now(), true, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700030001, -1700030001, 'B14LO', 1, '2019-12-14', '09:00', '3rd', now());

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-17000, -1700030001, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-2000000, -1700030001, 'd49323c0-04da-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'M', 'British', 'Polish');

INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-2000000, -2000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- For Single case and defendant ID save (3db9d70b-10a2-49d1-b74d-379f2db74862)
-- 2 hearings, 2 defendants
-- 1263de26-4a81-42d3-a798-bad802433318 - John Peel
-- 6f014c2e-8be3-4a12-a551-8377bd31a7b8 - Jessica Peel
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, awaiting_psr, source_type)
VALUES (-1800028900, '3db9d70b-10a2-49d1-b74d-379f2db74862', 1800028900, 'B33HU', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr John PEEL', '{"title": "Mr", "surname": "PEEL", "forename1": "John", "forename2": "Jonny", "forename3": "Jon"}','{"line1": "10", "line2": "Margrave", "postcode": "SU11 1AA", "line3": "Suffolk", "line4": null, "line5": null}', 'X320654', 'A/1234560YY', '888888/13E', '3rd', '1939-10-10', 'M', 'British', 'Irish' , now(), true, 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-3000000, -1800028900, 'B33HU', 1, '2019-12-14', '09:00', '3rd');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-3000001, -1800028900, 'B33HU', 1, '2019-12-15', '13:00', '3rd');

INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, awaiting_psr, probation_status)
VALUES (-3000000, -1800028900, '1263de26-4a81-42d3-a798-bad802433318', 'Mr John PEEL', '{"title": "Mr", "surname": "PEEL", "forename1": "John", "forename2": "Jonny", "forename3": "Jon"}', '{"line1": "10", "line2": "Margrave", "postcode": "SU11 1AA", "line3": "Suffolk", "line4": null, "line5": null}', 'PERSON', '1939-10-10', null, 'A/1234560YY', '888888/13E', 'M', 'British', 'Irish', true, null);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3000000, -3000000, 'John PEEL Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, awaiting_psr)
VALUES (-3000001, -1800028900, '6f014c2e-8be3-4a12-a551-8377bd31a7b8', 'Mrs Jessica PEEL', '{"title": "Mr", "surname": "PEEL", "forename1": "Jessica", "forename2": "Lisa", "forename3": "Julie"}', '{"line1": "11", "line2": "Margrave", "postcode": "SU11 1AA", "line3": "Suffolk", "line4": null, "line5": null}', 'PERSON', '1939-10-10', null, 'A/1234560ZZ', '999999/13E', 'F', 'British', 'Irish', true);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3000001, -3000001, 'Jessica PEEL Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Two matches for Mr John PEEL
INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE, CASE_ID, DEFENDANT_ID)
VALUES (-1800028900, '1800028900', 'B33HU', '3db9d70b-10a2-49d1-b74d-379f2db74862', '1263de26-4a81-42d3-a798-bad802433318');
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320654', 'NAME_DOB', 'A323456', -1800028900);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X999999', 'NAME_DOB', 'B323456', -1800028900);
--

-- One match for Jessica PEEL
INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE, CASE_ID, DEFENDANT_ID)
VALUES (-1800028901, '1800028900', 'B33HU', '3db9d70b-10a2-49d1-b74d-379f2db74862', '6f014c2e-8be3-4a12-a551-8377bd31a7b8');
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320654', 'NAME_DOB', 'A323456', -1800028901);
--

-- See CourtCaseControllerPutIntTest.whenCreateCaseExtendedByCaseId_thenCreateNewRecord() CHECKED
-- These records are used to test other cases with the same CRN getting updates on probation status. This is a different case with 2 versions, only the most recent will be updated
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, source_type, created)
VALUES (4000000, 'ce84bb2d-e44a-4554-a1a8-795accaac4d8', 4000000, 'B63AD', '1', '2100-01-01 09:00:00', 'COMMON_PLATFORM', NOW() - INTERVAL '3 day');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-4000000, 4000000, 'B63AD', 1, '2100-12-15', '13:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, crn, sex, probation_status, created)
VALUES (-4000000, 4000000, '27457a3e-fc49-49d3-af22-bf980df4a805', 'Ms Nicole KIDMAN', '{"title": "Ms", "surname": "KIDMAN", "forename1": "Nicole"}', 'PERSON', 'X320741', 'F', 'CURRENT', NOW() - INTERVAL '3 day');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, crn, sex, probation_status, created)
VALUES (-4000001, 4000000, '81adf9ee-76ab-42cc-998d-fb6ae80a4cc9', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'DX12345', 'M', 'CURRENT', NOW() - INTERVAL '3 day');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, session_start_time, source_type, created)
VALUES (4000001, 'ce84bb2d-e44a-4554-a1a8-795accaac4d8', 4000001, '2100-01-01 09:00:00', 'COMMON_PLATFORM', NOW() - INTERVAL '1 day');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-4000001, 4000001, 'B63AD', 1, '2100-12-15', '13:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, crn, sex, probation_status, created)
VALUES (-4000002, 4000001, '27457a3e-fc49-49d3-af22-bf980df4a805', 'Miss Nicole KIDMAN', '{"title": "Miss", "surname": "KIDMAN", "forename1": "Nicole"}', 'PERSON', 'X320741', 'F', 'CURRENT', NOW() - INTERVAL '1 day');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, type, crn, sex, probation_status, created)
VALUES (-4000003, 4000001, '81adf9ee-76ab-42cc-998d-fb6ae80a4cc9', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'DX12345', 'M', 'CURRENT', NOW() - INTERVAL '3 day');
