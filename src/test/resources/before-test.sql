TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.offence CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;

INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('North Shields', 'B10JQ');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Leicester', 'B33HU');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Aberystwyth', 'B63AD');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('New New York', 'B30NY');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, defendant_name, name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2, created) VALUES (1700028913, 5555555, 1600028913, 'B10JQ', 1, '2019-12-14 09:00', null, '2010-01-01', true, true, true, 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}','{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', null, 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' , now());

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnCasesForToday()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555556, 1600028914, 'B10JQ', 1,
'2019-12-14 00:00', 'NOT_SENTENCED', 'X320742','Mr Billy ZANE', now());
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555557, 1600028915, 'B10JQ', 1,
'2019-12-14 23:59:59', 'NO_RECORD', 'X320743', 'Mr Nicholas CAGE', now());

-- See CourtCaseControllerIntTest.GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime()
-- These records are used to test the createdAfter filters
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555558, 1600028916, 'B10JQ', 1, '2019-12-14 12:59:59', 'NO_RECORD', 'X320744', 'Mr Mads Mikkelsen',
'2020-09-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555559, 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', 'CURRENT', 'X320745', 'Mr Hideo Kojima',
'2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555559, 1600028917, 'B10JQ', 1,'2019-12-14 12:59:59', 'NO_RECORD', 'X320745', 'Mr Hideo Kojima',
'2020-10-01 18:59:59');


INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, created_by, manual_update) VALUES (5555560, 1600028919, 'B30NY', 1, '2019-12-14 12:59:59', 'NO_RECORD', 'X320654', 'Hubert Farnsworth',
'2020-10-01 16:59:59', 'TURANGALEE(prepare-a-case-for-court)', true);

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test that the createdToday field returns false if a new record was created today updating an existing one
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555559, 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', 'NO_RECORD', 'X320746', 'Mr David Bowie',
'2020-10-01 16:59:59');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created) VALUES (5555559, 1600028918, 'B10JQ', 2,'2019-12-14 13:00:00', 'NO_RECORD', 'X320746', 'Mr David Bowie',
now());

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These cases simulate when a case has been moved to a future date, in this case it should no longer appear in the old case list
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created)
VALUES (5555557, 1600028921, 'B10JQ', 1,'2020-02-14 13:00:00', 'No record', 'X320743', 'Mr Future', '2019-11-15 00:00:00');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created)
VALUES (5555557, 1600028921, 'B10JQ', 1,'2019-12-14 13:00:00', 'No record', 'X320743', 'Mr Future', '2019-11-14 00:00:00');

-- These records are used to test soft deletion
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted) VALUES (5555559, 1600128918, 'B10JQ', 2,'2019-12-14 13:00:00', 'No record', 'X320746', 'Mr David Bowie',
'2020-10-01 16:59:59', false);
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name, created, deleted) VALUES (5555559, 1600128918, 'B10JQ', 2,'2019-12-14 13:00:00', 'No record', 'X320746', 'Mr David Bowie',
now(), true);


-- See CourtCaseControllerPutIntTest.whenPurgeCases_ThenReturn204NoContent()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000000, 1000000, 1000000, 'B10JQ', '1', '2020-01-01 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000001, 1000001, 1000001, 'B10JQ', '1', '2020-01-01 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000002, 1000002, 1000002, 'B10JQ', '3', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000003, 1000003, 1000003, 'B10JQ', '2', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000004, 1000004, 1000004, 'B10JQ', '1', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000005, 1000005, 1000005, 'B10JQ', '3', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000006, 1000006, 1000006, 'B10JQ', '2', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000007, 1000007, 1000007, 'B10JQ', '1', '2020-01-02 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000008, 1000008, 1000008, 'B10JQ', '1', '2020-01-03 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000009, 1000009, 1000009, 'B10JQ', '1', '2020-01-03 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID,
                                          OFFENCE_TITLE,
                                          OFFENCE_SUMMARY,
                                          ACT,
                                          SEQUENCE_NUMBER)
VALUES (1700028913,
        'Theft from a shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1);

INSERT INTO courtcaseservicetest.OFFENCE (COURT_CASE_ID,
                                          OFFENCE_TITLE,
                                          OFFENCE_SUMMARY,
                                          ACT,
                                          SEQUENCE_NUMBER)
VALUES (1700028913,
        'Theft from a different shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        2);

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
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (15000, 15000, '15000', 'B10JQ', '1', NOW() + INTERVAL '1 day', 'No record', 'X320747');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (15001, 15001, '15001', 'B10JQ', '1', NOW() + INTERVAL '2 days', 'Current', 'X320747');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (15002, 15002, '15002', 'B10JQ', '1', NOW() - INTERVAL '7 days', 'No record', 'X320747');

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15000, 15000, 'Offence Title 15000', 'Offence Summary 15000', 'Offence ACT 15000', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15001, 15000, 'Offence Title 15001', 'Offence Summary 15001', 'Offence ACT 15001', 1);

INSERT INTO courtcaseservicetest.OFFENCE (ID, COURT_CASE_ID, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER)
VALUES (15002, 15001, 'Offence Title 15002', 'Offence Summary 15002', 'Offence ACT 15002', 1);
