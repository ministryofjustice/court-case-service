TRUNCATE offender_match_group CASCADE;
TRUNCATE offender_match CASCADE;
TRUNCATE offender CASCADE;
TRUNCATE hearing_day CASCADE;
TRUNCATE hearing CASCADE;
TRUNCATE hearing_defendant CASCADE;
TRUNCATE defendant CASCADE;
TRUNCATE court_case CASCADE;
TRUNCATE court CASCADE;
TRUNCATE case_comments CASCADE;

INSERT INTO court (name, court_code) VALUES ('North Shields', 'B10JQ');
INSERT INTO court (name, court_code) VALUES ('Sheffield Magistrates Court', 'B14LO');
INSERT INTO court (name, court_code) VALUES ('Leicester', 'B33HU');
INSERT INTO court (name, court_code) VALUES ('Aberystwyth', 'B63AD');
INSERT INTO court (name, court_code) VALUES ('New New York', 'B30NY');
INSERT INTO court (name, court_code) VALUES ('Old New York', 'C10JQ');

-- The offender records for all CRNs in here
INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000000, 'X781345', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test');
INSERT INTO OFFENDER (id, crn, pnc, cro, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000001, 'X320741', 'PNCINT007', 'CROINT007', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000002, 'X320742', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000003, 'X320743', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000004, 'X320744', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000005, 'X320745', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000006, 'X320746', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000007, 'X320654', 'CURRENT', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, breach, pre_sentence_activity, suspended_sentence_order, created_by)
VALUES (-1000008, 'C16000', 'PREVIOUSLY_KNOWN', '2010-01-01', true, true, true, 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000009, 'D16000', 'NOT_SENTENCED', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, created_by)
VALUES (-1000010, 'DX12345', 'CURRENT', 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000011, 'Y320741', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test.sql');
INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000012, 'Z320755', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test.sql');


-- START DEFINITION OF CASE NO 1600028913
INSERT INTO court_case (id, case_id, case_no, created, source_type, urn) VALUES (-1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 1600028913, now(), 'LIBRA', 'URN008');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, hearing_type, created, hearing_event_type, list_no) VALUES (-1700028900, -1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'sentence', now(), 'RESULTED', '3rd');
INSERT INTO case_comments(id, case_id, defendant_id, comment, "author", created, created_by, created_by_uuid) VALUES (-1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '40db17d6-04db-11ec-b2d8-0242ac130002', 'PSR in progress', 'Author One', now(), 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');
INSERT INTO case_comments(id, case_id, defendant_id, comment, "author", created, deleted, created_by, created_by_uuid) VALUES (-1700028901, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '20db17d6-04db-11ec-b2d8-0242ac130002', 'PSR completed', 'Author One', now(), true, 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');
INSERT INTO case_comments(id, case_id, defendant_id, comment, "author", created, created_by, created_by_uuid, legacy) VALUES (-1700028902, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '40db17d6-04db-11ec-b2d8-0242ac130002', 'PSR completed', 'Author Two', now(), 'before-test.sql', '389fd9cf-390e-469a-b4cf-6c12024c4cae', true);
INSERT INTO case_comments(id, case_id, defendant_id, comment, "author", created, created_by, created_by_uuid, is_draft) VALUES (-1700028903, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '40db17d6-04db-11ec-b2d8-0242ac130002', 'PSR completed', 'Author Two', now(), 'before-test.sql', '389fd9cf-390e-469a-b4cf-6c12024c4cae', true);

INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00');
INSERT INTO DEFENDANT(id, DEFENDANT_ID,PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, fk_offender_id, pnc, cro, sex, nationality_1, nationality_2, phone_number, cpr_uuid)
VALUES (-1000000, '40db17d6-04db-11ec-b2d8-0242ac130002','b875f962-4b95-11ed-bdc3-0242ac120002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X320741', -1000001, 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', '{"home": "07000000013", "mobile": "07000000007", "work": "07000000015"}', 'cd33edce-5948-4592-a4ac-b5eb48d01209');

INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1000000, -1700028900, '40db17d6-04db-11ec-b2d8-0242ac130002', -1000000);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, LIST_NO)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, 10);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

-- END DEFINITION OF 1600028913

-- See CourtCaseControllerIntTest.GET_cases_givenNoCreatedFilterParams_whenGetCases_thenReturnAllCases()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700029901, '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', 1600028914, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700029901, -1700029901, '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', now());
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700029901, -1700029901, 'B10JQ', 1, '2019-12-14', '07:00');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700029902, -1700029901, 'B10JQ', 2, '2019-12-15', '13:00');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2, phone_number)
VALUES (-1700029902, '7a319a46-037c-481c-ab1e-dbfab62af4d6', 'fdbaa5d6-4b95-11ed-bdc3-0242ac120002', 'Mr Billy ZANE', '{"title": "Mr", "surname": "ZANE", "forename1": "Billy"}', '{"line1": "Billy Place", "line2": "Zane Pla", "postcode": "z12 8gt 5dr"}', 'PERSON', '1987-10-10', 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', '{"home": "07000000013", "mobile": "07000000008", "work": "07000000015"}');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700029902, -1700029901, '7a319a46-037c-481c-ab1e-dbfab62af4d6', -1700029902);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1700029902, -1700029902, 'Billy stole from a shop', 'On 01/01/2015 at own, Billy stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);
INSERT INTO DEFENDANT(id, DEFENDANT_ID,PERSON_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2, phone_number)
VALUES (-1700029903, '7a320a46-037c-481c-ab1e-dbfab62af4d6', '0274f01c-4b97-11ed-bdc3-0242ac120002', 'Ms Emma Radical', '{"title": "Ms", "surname": "RADICAL", "forename1": "Emma"}', '{"line1": "Emma Place", "line2": "Radical Place", "postcode": "e12 8gt"}', 'PERSON', '1987-10-10', 'A/1234560CD', '311465/13F', 'FEMALE', 'Romanian', 'Chinese', '{"home": "07000000013", "mobile": "07000000006", "work": "07000000015"}');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700029903, -1700029901, '7a320a46-037c-481c-ab1e-dbfab62af4d6', -1700029903);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, LIST_NO, OFFENCE_CODE)
VALUES (-1700029903, -1700029903, 'Emma stole 1st thing from a shop', 'On 01/01/2015 at own, Emma stole 1st article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, 35, 'RT88191');
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, OFFENCE_CODE)
VALUES (-1700029904, -1700029903, 'Emma stole 2nd thing from a shop', 'On 01/01/2015 at own, Emma stole 2nd article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2, 'RT88191');
INSERT INTO case_marker (id, FK_COURT_CASE_ID, type_description, created)
VALUES (-1700029901, -1700029901, 'description 1', now());

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028902, '1f93aa0a-7e46-4885-a1cb-f25a4be33a57', 1600028915, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028902, -1700028902,'1f93aa0a-7e46-4885-a1cb-f25a4be33a57', now());
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700028902, -1700028902, 'B10JQ', 1, '2019-12-14', '23:59:59');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1700028902, 'f15bd32c-119d-447e-bbb6-02b56c36f133', '1b1a6c96-4b97-11ed-bdc3-0242ac120002', 'Mr Nicholas CAGE', '{"title": "Mr", "surname": "CAGE", "forename1": "Nicholas"}', 'X320743', -1000003, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028902, -1700028902, 'f15bd32c-119d-447e-bbb6-02b56c36f133', -1700028902);

-- See CourtCaseControllerIntTest.GET_cases_givenCreatedAfterFilterParam_whenGetCases_thenReturnCasesAfterSpecifiedTime()
-- These records are used to test the createdAfter filters
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028903, '1f93aa0a-7e46-4885-a1cb-f25a4be33a58', 1600028916, '2020-09-01 16:59:59', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028903, -1700028903, '1f93aa0a-7e46-4885-a1cb-f25a4be33a58', '2020-09-01 16:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700028903, -1700028903, 'B10JQ', 1, '2019-12-14', '12:59:59');
INSERT INTO DEFENDANT(id, DEFENDANT_ID,PERSON_ID, defendant_name, name, crn, type, sex)
VALUES (-1700028903, '44817de0-cc89-460a-8f07-0b06ef45982a', '1b1a6c96-4b97-11ed-bdc3-0242ac120002', 'Mr Mads MIKKELSEN', '{"title": "Mr", "surname": "MIKKELSEN", "forename1": "Mads"}', 'X320744', 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028903, -1700028903, '44817de0-cc89-460a-8f07-0b06ef45982a', -1700028903);

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028905, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', 1600028917, '2020-10-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028905, -1700028905, '1f93aa0a-7e46-4885-a1cb-f25a4be33a59', '2020-10-01 18:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700028905, -1700028905, 'B10JQ', 1, '2019-12-14', '12:59:59');
INSERT INTO DEFENDANT(id, DEFENDANT_ID,PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1700028905, '965c0391-8929-4fff-b88c-2f813cf16d43', '1b1a6c96-4b97-11ed-bdc3-0242ac120002', 'Mr Hideo KOJIMA', '{"title": "Mr", "surname": "KOJIMA", "forename1": "Hideo"}', 'X320745', -1000005, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028905, -1700028905, '965c0391-8929-4fff-b88c-2f813cf16d43', -1700028905);

-- See GET_cases_givenCreatedBeforeFilterParam_whenGetCases_thenReturnCasesCreatedUpTo8DaysBeforeListDate
-- Used to test default createdBefore date
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028906, '1f93aa0a-7e46-4885-a1cb-f25a4be33a30', 1600028930, '2020-05-01 18:59:59', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1700028906, -1700028906, '1f93aa0a-7e46-4885-a1cb-f25a4be33a30', '2020-05-01 18:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1700028906, -1700028906, 'B10JQ', 1, '2020-05-01', '12:59:59');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028906, -1700028906, '965c0391-8929-4fff-b88c-2f813cf16d43', -1700028905);

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700028908, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', 1600028918, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, first_created)
VALUES (-1700028908, -1700028908, '1f93aa0a-7e46-4885-a1cb-f25a4be33a18', now(), '2020-10-01 16:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700028908, -1700028908, 'B10JQ', 2, '2019-12-14', '13:00:00', now());
INSERT INTO DEFENDANT(id, DEFENDANT_ID,PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1700028908, '3bf70cd8-7e9d-4d29-b9b2-f8f7f898cb32', 'e6adb0c0-4b97-11ed-bdc3-0242ac120002', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'X320746', -1000006, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028908, -1700028908, '3bf70cd8-7e9d-4d29-b9b2-f8f7f898cb32', -1700028908);

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1800028909, 'c4dee5e1-3cd9-4a2d-9e39-ccdacefc92b9', 1700028918, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, first_created)
VALUES (-1800028909, -1800028909, '682da1fb-cdce-476c-adff-0a2bd9f8355a', now(), '2020-10-01 16:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1800028909, -1800028909, 'C10JQ', 2, '2019-12-14', '13:00:00', now());
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1800028909, '9c2f11b0-1bca-4b24-85a1-315d67020b2c', 'f6144056-4b97-11ed-bdc3-0242ac120002', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'Y320741', -1000011, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1800028909, -1800028909, '9c2f11b0-1bca-4b24-85a1-315d67020b2c', -1800028909);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1800028901, 'd59762b6-2da7-4af0-a09f-7296d40f15ce', '08035aae-4b98-11ed-bdc3-0242ac120002', 'Mr Phil BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'Y320741', -1000011, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1800028901, -1800028909, 'd59762b6-2da7-4af0-a09f-7296d40f15ce', -1800028901);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, type, sex)
VALUES (-1800028902, 'c34bfca0-1ff1-4dab-9db7-acd27392b31a', '195684e8-4b98-11ed-bdc3-0242ac120002', 'Mr Phil BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1800028902, -1800028909, 'c34bfca0-1ff1-4dab-9db7-acd27392b31a', -1800028902);

-- See GET_cases_givenCreatedBefore_andCreatedAfterFilterParams_andManualUpdatesHaveBeenMadeAfterTheseTimes_whenGetCases_thenReturnManualUpdates
INSERT INTO court_case (id, case_id, case_no, created, created_by, source_type)
VALUES (-1700028909, 'e652eaae-1114-4593-8f56-659eb2baffcf', 1600028919, '2020-10-01 16:59:59', 'TURANGALEE(prepare-a-case-for-court)', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, created_by)
VALUES (-1700028909, -1700028909, 'e652eaae-1114-4593-8f56-659eb2baffcf', '2020-10-01 16:59:59', 'TURANGALEE(prepare-a-case-for-court)');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700028909, -1700028909, 'B30NY', 1, '2200-12-14', '12:59:59', '2020-10-01 16:59:59');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, pnc, sex, crn, fk_offender_id)
VALUES (-1700028909, 'c15475ce-9748-4a60-b42b-02ce78523c95', '2bbfaf60-4b98-11ed-bdc3-0242ac120002', 'Mr Hubert FARNSWORTH', '{"title": "Mr", "surname": "FARNSWORTH", "forename1": "Hubert"}', '{"line1": "Anfield", "line2": "Walton Breck Road", "postcode": "L5 2DE 5dr"}', 'PERSON', '1940-05-01', 'A/1234560BA', 'MALE', 'X320654', -1000007);
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028909, -1700028909, 'c15475ce-9748-4a60-b42b-02ce78523c95', -1700028909);
-- These records are used to test the Last-Modified header CHECKED
/*INSERT INTO court_case (id, case_id, case_no, created, deleted, source_type)
VALUES (-1700028910, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, '2020-10-01 16:59:59', false, 'LIBRA');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, deleted)
VALUES (-1700028910, -1700028910, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', '2020-10-01 16:59:59', false);
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700028910, -1700028910, 'B14LO', 1, '2021-06-01', '13:00:00', '2020-10-01 16:59:59');
*/
INSERT INTO court_case (id, case_id, case_no, created, deleted, source_type)
VALUES (-1700028911, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', 1600128919, '2020-10-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, deleted)
VALUES (-1700028911, -1700028911, '1f93aa0a-7e46-4885-a1cb-f25a4be33a60', '2020-10-01 16:59:59', false);
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700028911, -1700028911, 'B14LO', 2, '2021-06-01', '13:00:00', '2020-10-01 16:59:59');

INSERT INTO court_case (id, case_id, case_no, created, deleted, source_type)
VALUES (-1700028912, '1f93aa0a-7e46-4885-a1cb-f25a4be33a20', 1600128920, '2021-06-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, deleted)
VALUES (-1700028912, -1700028912, '1f93aa0a-7e46-4885-a1cb-f25a4be33a20', '2021-06-01 16:59:59', false);
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700028912, -1700028912, 'B14LO', 2, '2021-06-01', '13:00:00', '2021-06-01 16:59:59');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, crn, fk_offender_id, type, sex)
VALUES (-1700028912, '03137ac2-8c92-471a-aed2-c92ea6e4963e', '3d24be4e-4b98-11ed-bdc3-0242ac120002', 'Mr George O''DOWD', '{"title": "Mr", "surname": "O''DOWD", "forename1": "George"}', 'X320746', -1000006, 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-1700028912, -1700028912, '03137ac2-8c92-471a-aed2-c92ea6e4963e', -1700028912);

-- See CourtCaseControllerPutIntTest.whenPurgeCases_ThenReturn204NoContent() CHECKED
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000000, 1000000, 1000000, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000000, 1000000, 1000000);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000001, 1000001, 1000001, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000001, 1000001, 1000001);

INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000002, 1000002, 1000002, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000002, 1000002, 1000002);
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID)
VALUES (-1000002, 1000002, '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000003, 1000003, 1000003, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000003, 1000003, 1000003);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000004, 1000004, 1000004, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000004, 1000004, 1000004);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000005, 1000005, 1000005, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000005, 1000005, 1000005);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000006, 1000006, 1000006, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000006, 1000006, 1000006);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000007, 1000007, 1000007, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000007, 1000007, 1000007);

INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000008, 1000008, 1000008, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000008, 1000008, 1000008);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (1000009, 1000009, 1000009, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (1000009, 1000009, 1000009);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999991, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999992, '1f93aa0a-7e46-4885-a1cb-f25a4be33a56', '7a320a46-037c-481c-ab1e-dbfab62af4d6');


INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320741', '12345', 'NAME_DOB', '123456', 9999991);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, '2234', '22345', 'NAME_DOB', '223456', 9999991);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (true, false, 'X6666', '78654', 'NAME_DOB', '323456', 9999991);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999998, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'b6e663c5-d2be-434b-b597-3e98a112af9f');

INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320741', '12345', 'NAME_DOB', '123456', 9999998);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, '2234', '22345', 'NAME_DOB', '223456', 9999998);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (true, false, 'X6666', '78654', 'NAME_DOB', '323456', 9999998);

INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, '3234', '32345', 'NAME_DOB', '323456', 9999992);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, 'CRN123', '32345', 'NAME_DOB', 'PNC12/456', 9999992);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999993, '1000002', '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', '323456', 9999993);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999995, '1000002', 'b6e663c5-d2be-434b-b597-3e98a112af9f');
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', '323456', 9999995);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (9999996, '1000002', 'b3f7e2bc-c8f9-4031-9c0c-928c41193acd');
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', '323456', 9999996);

-- See CourtCaseControllerPutIntTest.whenUpdateCaseDataByCourtAndCaseNo_ThenUpdateProbationStatusOnCasesWithSameCrn()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (15000, 15000, '15000', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (15000, 15000, 15000);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (15001, 15001, '15001', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (15001, 15001, 15001);
INSERT INTO court_case (id, case_id, case_no, source_type)
VALUES (15002, 15002, '15002', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id)
VALUES (15002, 15002, 15002);

--
-- These records are used to generate cases / offences / matches for the PACT verification tests
-- We will have 3 case sections in the case ist but with 2 unique case IDs and 3 defendant IDs
-- Case id 1 : 683bcde4-611f-4487-9833-f68090507b74, defendants 005ae89b-46e9-4fa5-bb5e-d117011cab32 and f2c83643-8ebd-4609-9183-cd8c34984e33
-- Case id 2 : 2243231a-7810-496c-bd41-cb01ceb1fe0b, defendant bfd7df09-4177-475e-b16f-0ace34a5ef2f
INSERT INTO court_case (id, case_id, case_no, created, created_by, source_type) VALUES
(16000, '683bcde4-611f-4487-9833-f68090507b74', 16000, now(), 'ChrisFaulkner(manual-PIC-959)', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, created_by) VALUES
(16000, 16000, '683bcde4-611f-4487-9833-f68090507b74', now(), 'ChrisFaulkner(manual-PIC-959)');
INSERT INTO court_case (id, case_id, case_no, created, created_by, source_type) VALUES
(16001, '2243231a-7810-496c-bd41-cb01ceb1fe0b', 16001, now(), 'ChrisFaulkner(manual-PIC-959)', 'LIBRA');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, created_by) VALUES
(16001, 16001, '2243231a-7810-496c-bd41-cb01ceb1fe0b', now(), 'ChrisFaulkner(manual-PIC-959)');

-- 683bcde4-611f-4487-9833-f68090507b74 - Hearing / defendants / defendant offences for case id
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-16000, 16000, 'B10JQ', 1, '2020-02-29', '14:00:00');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, fk_offender_id, pnc, sex, nationality_1, nationality_2, phone_number)
VALUES (-16000, '005ae89b-46e9-4fa5-bb5e-d117011cab32','14bb6708-4b95-11ed-bdc3-0242ac120002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": "line4", "line5": "line5"}', 'PERSON', '1958-10-10', 'C16000', -1000008, 'A/160000BA', 'MALE', 'British', 'Polish', '{"home": "07000000013", "mobile": "07000000008", "work": "07000000015"}');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-16000, 16000, '005ae89b-46e9-4fa5-bb5e-d117011cab32', -16000);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, fk_offender_id, pnc, sex, nationality_1)
VALUES (-16001, 'f2c83643-8ebd-4609-9183-cd8c34984e33', '209ff0b6-4b9a-11ed-bdc3-0242ac120002', 'Mr Brian CANT', '{"title": "Mr", "surname": "CANT", "forename1": "Brian", "forename2": "Bryan", "forename3": "Eric"}', '{"line1": "26", "line2": "Elms Road", "postcode": "LE2 3LU", "line3": "Leicester"}', 'PERSON', '1939-10-10', 'D16000', -1000009, 'D/160000BA', 'MALE', 'British');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-16001, 16000, 'f2c83643-8ebd-4609-9183-cd8c34984e33', -16001);

INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-16000, -16000, 'Offence Title 16000', 'Offence Summary 16000', 'Offence ACT 16000', 1);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-16001, -16001, 'Offence Title 16001', 'Offence Summary 16001', 'Offence ACT 16001', 1);

-- 2243231a-7810-496c-bd41-cb01ceb1fe0b - Hearing / defendants / defendant offences for case id
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-16001, 16001, 'B10JQ', 10, '2020-02-29', '14:00:00');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, sex, type)
VALUES (-16002, 'bfd7df09-4177-475e-b16f-0ace34a5ef2f', '412b0b7c-4b9a-11ed-bdc3-0242ac120002', 'ACME MOTORS LTD', '{"surname": "ACME MOTORS LTD"}', '{"line1": "Freemans Common", "line2": "Someplace", "postcode": "XX1 1XX", "line3": "Leicester"}', 'NOT_KNOWN', 'ORGANISATION');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-16002, 16001, 'bfd7df09-4177-475e-b16f-0ace34a5ef2f', -16002);

INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (16000, '683bcde4-611f-4487-9833-f68090507b74','005ae89b-46e9-4fa5-bb5e-d117011cab32');

INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', 'A/160000BA', 16000);

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1700030001, 'ac24a1be-939b-49a4-a524-21a3d2230000', '1700030000', now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-1700030001, -1700030001, 'ac24a1be-939b-49a4-a524-21a3d2230000', now(), '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-1700030001, -1700030001, 'B14LO', 1, '2019-12-14', '09:00', now());

INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-2000000, 'd49323c0-04da-11ec-b2d8-0242ac130002', '5dbbb502-4b9a-11ed-bdc3-0242ac120002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-2000000, -1700030001, 'd49323c0-04da-11ec-b2d8-0242ac130002', -2000000);

INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-2000000, -2000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- For Single case and defendant ID save (3db9d70b-10a2-49d1-b74d-379f2db74862)
-- 2 hearings, 2 defendants
-- 1263de26-4a81-42d3-a798-bad802433318 - John Peel
-- 6f014c2e-8be3-4a12-a551-8377bd31a7b8 - Jessica Peel
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-1800028900, '3db9d70b-10a2-49d1-b74d-379f2db74862', 1800028900, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-1800028900, -1800028900, '3db9d70b-10a2-49d1-b74d-379f2db74862', now());

INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-3000000, -1800028900, 'B33HU', 1, '2019-12-14', '09:00');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-3000001, -1800028900, 'B33HU', 1, '2019-12-15', '13:00');

INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-3000000, '1263de26-4a81-42d3-a798-bad802433318', '7a11e9c4-4b9a-11ed-bdc3-0242ac120002','Mr John PEEL', '{"title": "Mr", "surname": "PEEL", "forename1": "John", "forename2": "Jonny", "forename3": "Jon"}', '{"line1": "10", "line2": "Margrave", "postcode": "SU11 1AA", "line3": "Suffolk", "line4": null, "line5": null}', 'PERSON', '1939-10-10', 'A/1234560YY', '888888/13E', 'MALE', 'British', 'Irish');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-3000000, -1800028900, '1263de26-4a81-42d3-a798-bad802433318', -3000000);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3000000, -3000000, 'John PEEL Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, pnc, cro, sex, nationality_1, nationality_2)
VALUES (-3000001, '6f014c2e-8be3-4a12-a551-8377bd31a7b8', '8c371c32-4b9a-11ed-bdc3-0242ac120002', 'Mrs Jessica PEEL', '{"title": "Mr", "surname": "PEEL", "forename1": "Jessica", "forename2": "Lisa", "forename3": "Julie"}', '{"line1": "11", "line2": "Margrave", "postcode": "SU11 1AA", "line3": "Suffolk", "line4": null, "line5": null}', 'PERSON', '1939-10-10', 'A/1234560ZZ', '999999/13E', 'FEMALE', 'British', 'Irish');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-3000001, -1800028900, '6f014c2e-8be3-4a12-a551-8377bd31a7b8', -3000001);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-3000001, -3000001, 'Jessica PEEL Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1);

-- Two matches for Mr John PEEL
INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (-1800028900, '3db9d70b-10a2-49d1-b74d-379f2db74862', '1263de26-4a81-42d3-a798-bad802433318');
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320654', 'NAME_DOB', 'A323456', -1800028900);
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X999999', 'NAME_DOB', 'B323456', -1800028900);
--

-- One match for Jessica PEEL
INSERT INTO offender_match_group(ID, CASE_ID, DEFENDANT_ID)
VALUES (-1800028901, '3db9d70b-10a2-49d1-b74d-379f2db74862', '6f014c2e-8be3-4a12-a551-8377bd31a7b8');
INSERT INTO offender_match(CONFIRMED, REJECTED, CRN, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320654', 'NAME_DOB', 'A323456', -1800028901);
--

-- See CourtCaseControllerPutIntTest.whenCreateCaseExtendedByCaseId_thenCreateNewRecord() CHECKED
-- These records are used to test other cases with the same CRN getting updates on probation status. This is a different case with 2 versions, only the most recent will be updated
INSERT INTO court_case (id, case_id, case_no, source_type, created)
VALUES (4000000, 'ce84bb2d-e44a-4554-a1a8-795accaac4d8', 4000000, 'COMMON_PLATFORM', NOW() - INTERVAL '3 day');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (4000000, 4000000, 'ce84bb2d-e44a-4554-a1a8-795accaac4d8', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-4000000, 4000000, 'B63AD', 1, '2100-12-15', '13:00');
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID,defendant_name, name, type, crn, fk_offender_id, sex, created)
VALUES (-4000000, '27457a3e-fc49-49d3-af22-bf980df4a805', '9f86ed26-4b9a-11ed-bdc3-0242ac120002', 'Ms Nicole KIDMAN', '{"title": "Ms", "surname": "KIDMAN", "forename1": "Nicole"}', 'PERSON', 'X320741', -1000001, 'FEMALE', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-4000000, 4000000, '27457a3e-fc49-49d3-af22-bf980df4a805', -4000000);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, type, crn, fk_offender_id, sex, created)
VALUES (-4000001, '81adf9ee-76ab-42cc-998d-fb6ae80a4cc9', 'b292d7cc-4b9a-11ed-bdc3-0242ac120002', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'DX12345', -1000010, 'MALE', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-4000001, 4000000, '81adf9ee-76ab-42cc-998d-fb6ae80a4cc9', -4000001);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, type, sex, created)
VALUES (-4000002, '03d0c6a4-b00f-499b-bbb6-1fa80b1d7cf4', 'c936350a-4b9a-11ed-bdc3-0242ac120002', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'MALE', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-4000004, 4000000, '03d0c6a4-b00f-499b-bbb6-1fa80b1d7cf4', -4000001);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, type, crn, sex, created)
VALUES (-4000003, '7420ce9b-8d56-4019-9e68-81a17f54327e', 'de7cd450-4b9a-11ed-bdc3-0242ac120002', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'XXXXXXXX', 'MALE', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-4000005, 4000000, '7420ce9b-8d56-4019-9e68-81a17f54327e', -4000003);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, type, crn, sex, created)
VALUES (-4000006, 'af9f884d-22ec-4eaa-b420-9619c011afe6', 'f0446982-4b9a-11ed-bdc3-0242ac120002', 'Mr Tom CRUISE', '{"title": "Mr", "surname": "CRUISE", "forename1": "Tom"}', 'PERSON', 'Z320755', 'MALE', NOW() - INTERVAL '3 day');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-4000006, 4000000, 'af9f884d-22ec-4eaa-b420-9619c011afe6', -4000006);

INSERT INTO court_case (id, case_id, case_no, created, deleted, source_type)
VALUES (4000011, '1b6cf731-1892-4b9e-abc3-7fab87a39c21', 1111128919, '2020-10-01 16:59:59', false, 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (4000011, 4000011, '1b6cf731-1892-4b9e-abc3-7fab87a39c21', NOW() - INTERVAL '1 day');

INSERT INTO courtcaseservicetest.case_defendant(id, fk_court_case_id, fk_case_defendant_id)
VALUES (-1900020002, -1700028900, -1000000);

INSERT INTO courtcaseservicetest.case_defendant_documents(id, fk_case_defendant_id, document_id, document_name, created)
VALUES (-1800020002, -1900020002, '3cfd7d45-6f62-438e-ad64-ef3d911dfe38', 'test-upload-file-get.txt', '2024-03-01 16:59:59');

INSERT INTO courtcaseservicetest.case_defendant_documents(id, fk_case_defendant_id, document_id, document_name, created)
VALUES (-1800020003, -1900020002, '042bab62-afa6-4409-9d51-6cdf6d05bd04', 'test-upload-file-get-two.txt', '2024-03-02 16:59:59');