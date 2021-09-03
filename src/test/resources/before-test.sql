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
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, awaiting_psr, source_type) VALUES (-1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 1600028913, 'B10JQ', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', null, 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now(), true, 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028900, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (-1700028900, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT (id, court_case_id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-1000000, -1700028900, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'M', 'British', 'Polish');
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO courtcaseservicetest.DEFENDANT_OFFENCE (ID, DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);
-- END DEFINITION OF 1700028913

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028901, '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', 1600028914, 'B10JQ', 1, '2019-12-14 00:00', 'NOT_SENTENCED', 'X320742','Mr Billy ZANE', now(), 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028901, -1700028901, 'B10JQ', 1, '2019-12-14', '00:00', '3rd');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028902, '1f93aa0a-7e46-4885-a1cb-f25a4be33a57', 1600028915, 'B10JQ', 1, '2019-12-14 23:59:59', 'NO_RECORD', 'X320743', 'Mr Nicholas CAGE', now(), 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028902, -1700028902, 'B10JQ', 1, '2019-12-14', '23:59:59', '3rd');

-- See CourtCaseControllerIntTest.GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime()
-- These records are used to test the createdAfter filters
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028903, '1f93aa0a-7e46-4885-a1cb-f25a4be33a58', 1600028916, 'B10JQ', 1, '2019-12-14 12:59:59', 'NO_RECORD', 'X320744', 'Mr Mads Mikkelsen', '2020-09-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028903, -1700028903, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');

-- 2 versions for case id '1f93aa0a-7e46-4885-a1cb-f25a4be33a58'
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028904, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', 'CURRENT', 'X320745', 'Mr Hideo Kojima', '2020-10-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028904, -1700028904, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028905, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', 'NO_RECORD', 'X320745', 'Mr Hideo Kojima', '2020-10-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028905, -1700028905, 'B10JQ', 1, '2019-12-14', '12:59:59', '3rd');

-- See GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate
-- Used to test default createdBefore date
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028906, '1f93aa0a-7e46-4885-a1cb-f25a4be33a30', 1600028930, 'B10JQ', 1,'2020-05-01 12:59:59', 'NO_RECORD', 'X320745', 'Mr Hideo Kojima', '2020-05-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1700028906, -1700028906, 'B10JQ', 1, '2020-05-01', '12:59:59', '3rd');

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test that the createdToday field returns false if a new record was created today updating an existing one
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028907, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', 'NO_RECORD', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028907, -1700028907, 'B10JQ', 2, '2019-12-14', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, source_type)
VALUES (-1700028908, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', 'NO_RECORD', 'X320746', 'Mr David Bowie', now(), 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028908, -1700028908, 'B10JQ', 2, '2019-12-14', '13:00:00', '3rd', now());

-- See GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, created_by, manual_update, source_type)
VALUES (-1700028909, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600028919, 'B30NY', 1, '2019-12-14 12:59:59', 'NO_RECORD', 'X320654', 'Hubert Farnsworth', '2020-10-01 16:59:59', 'TURANGALEE(prepare-a-case-for-court)', true, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028909, -1700028909, 'B30NY', 1, '2019-12-14', '12:59:59', '3rd', '2020-10-01 16:59:59');

-- These records are used to test the Last-Modified header CHECKED
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted, source_type)
VALUES (-1700028910, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, 'B14LO', 2,'2021-06-01 13:00:00', 'No record', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', false, 'LIBRA');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028910, -1700028910, 'B14LO', 1, '2021-06-01', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted, source_type)
VALUES (-1700028911, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, 'B14LO', 2,'2021-06-01 13:00:00', 'No record', 'X320746', 'Mr David Bowie', '2020-10-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028911, -1700028911, 'B14LO', 2, '2021-06-01', '13:00:00', '3rd', '2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted, source_type)
VALUES (-1700028912, '1f93aa0a-7e46-4885-a1cb-f25a4be33a20', 1600128920, 'B14LO', 2,'2021-06-01 13:00:00', 'No record', 'X320746', 'Mr David Bowie', '2021-06-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.HEARING (id, court_case_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-1700028912, -1700028912, 'B14LO', 2, '2021-06-01', '13:00:00', '3rd', '2021-06-01 16:59:59');

-- See CourtCaseControllerPutIntTest.whenPurgeCases_ThenReturn204NoContent() CHECKED
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000000, 1000000, 1000000, 'B10JQ', '1', '2100-01-01 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000001, 1000001, 1000001, 'B10JQ', '1', '2020-01-01 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, source_type)
VALUES (1000002, 1000002, 1000002, 'B10JQ', '3', '2020-01-02 09:00:00', 'No record', 'X320741', 'COMMON_PLATFORM');
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


INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999991, '1600028913','B10JQ');
INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999992, '1600028914','B10JQ');


INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320741', '12345', 'NAME_DOB', '123456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, '2234', '22345', 'NAME_DOB', '223456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (true, false, 'X6666', '78654', 'NAME_DOB', '323456', 9999991);

INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, '3234', '32345', 'NAME_DOB', '323456', 9999992);

INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999993, '1000002','B10JQ');
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
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_type, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, created_by, source_type) VALUES (16000, 16000, 16000, 'B10JQ', 1, '2020-02-29 09:00', 'NOT_SENTENCED', '2010-01-01', true, true, true, 'PERSON', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": "line4", "line5": "line5"}', 'C16000', 'A/160000BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now(), 'ChrisFaulkner(manual-PIC-959)', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_type, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, created_by, source_type) VALUES (16001, 16001, 16001, 'B10JQ', 1, '2020-02-29 14:00', null, null, false, null, false, 'PERSON', 'Mr Brian CANT', '{"title": "Mr", "surname": "CANT", "forename1": "Brian", "forename2": "Bryan", "forename3": "Eric"}','{"line1": "26", "line2": "Elms Road", "postcode": "LE2 3LU", "line3": "Leicester"}', 'D16000', 'D/160000BA', '311462/13D', '1st', '1939-10-10', 'M', 'British',null , now(), 'ChrisFaulkner(manual-PIC-959)', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_type, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created, created_by, source_type) VALUES (16002, 16002, 16002, 'B10JQ', 10, '2020-02-29 14:00', null, null, null, null, false, 'ORGANISATION', 'ACME MOTORS LTD', '{"surname": "ACME MOTORS LTD"}','{"line1": "Freemans Common", "line2": "Someplace", "postcode": "XX1 1XX", "line3": "Leicester"}', null, null, null, '1st', null, null, null, null , now(), '(court-case-matcher)', 'COMMON_PLATFORM');

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (16000, 16000, 'Offence Title 16000', 'Offence Summary 16000', 'Offence ACT 16000', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (16001, 16001, 'Offence Title 16001', 'Offence Summary 16001', 'Offence ACT 16001', 1);

INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (16000, '16000','B10JQ');

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
