TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.offence CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;

INSERT INTO courtcaseservicetest.court (id, name, court_code) VALUES (4444443, 'Sheffield Magistrates Court Test', 'SHF');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (5555555, 1600028913, 'SHF', 1, '2019-12-14 09:00', 'Previously known', '2019-12-14 09:00', '2010-01-01', true, true, 'Mr Johnny BALL', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'X320741', 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' );

INSERT INTO courtcaseservicetest.OFFENCE (
	CASE_NO,
	COURT_CODE,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1600028913,
        'SHF',
        'Theft from a shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1
	);

INSERT INTO courtcaseservicetest.OFFENCE (
	CASE_NO,
	COURT_CODE,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1600028913,
        'SHF',
        'Theft from a different shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        2
	);

-- See CourtCaseControllerTest.shouldGetCaseListWhenCasesExist()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name) VALUES (5555556, 1600028914, 'SHF', 1,
'2019-12-14 00:00', 'No record', 'X320742','Mr Billy ZANE');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn, defendant_name) VALUES (5555557, 1600028915, 'SHF', 1,
'2019-12-14 23:59:59', 'No record', 'X320743', 'Mr Nicholas CAGE');


-- See CourtCaseControllerPutIntTest.whenPurgeCases_ThenReturn204NoContent()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000000, 1000000, 1000000, 'SHF', '1', '2020-01-01 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000001, 1000001, 1000001, 'SHF', '1', '2020-01-01 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000002, 1000002, 1000002, 'SHF', '3', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000003, 1000003, 1000003, 'SHF', '2', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000004, 1000004, 1000004, 'SHF', '1', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000005, 1000005, 1000005, 'SHF', '3', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000006, 1000006, 1000006, 'SHF', '2', '2020-01-02 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000007, 1000007, 1000007, 'SHF', '1', '2020-01-02 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000008, 1000008, 1000008, 'SHF', '1', '2020-01-03 09:00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, court_code, court_room, session_start_time, probation_status, crn)
VALUES (1000009, 1000009, 1000009, 'SHF', '1', '2020-01-03 09:00:00', 'No record', 'X320741');

INSERT INTO courtcaseservicetest.OFFENCE (ID, CASE_NO, COURT_CODE, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER	)
VALUES (1000001, 1000001, 'SHF', 'Title', 'Summary.', 'ACT.', 1);
INSERT INTO courtcaseservicetest.OFFENCE (ID, CASE_NO, COURT_CODE, OFFENCE_TITLE, OFFENCE_SUMMARY, ACT, SEQUENCE_NUMBER	)
VALUES (1000002, 1000002, 'SHF', 'Title', 'Summary.', 'ACT.', 2);


INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999991, '1600028913','SHF');
INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999992, '1600028914','SHF');


INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X320741', '12345', 'NAME_DOB', '123456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, '2234', '22345', 'NAME_DOB', '223456', 9999991);
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (true, false, 'X6666', '78654', 'NAME_DOB', '323456', 9999991);

INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, true, '3234', '32345', 'NAME_DOB', '323456', 9999992);

INSERT INTO courtcaseservicetest.offender_match_group(ID, CASE_NO, COURT_CODE)
VALUES (9999993, '1000002','SHF');
INSERT INTO courtcaseservicetest.offender_match(CONFIRMED, REJECTED, CRN, CRO, MATCH_TYPE, PNC, GROUP_ID)
VALUES (false, false, 'X980123', 'CRO1', 'NAME_DOB', '323456', 9999993);


INSERT INTO immutable_offence (created, created_by, deleted, version, act, offence_summary, offence_title,
                               sequence_number, court_case_id)
SELECT o.created,o.created_by,o.deleted,1,o.act,o.offence_summary,o.offence_title,o.sequence_number,c.id
FROM offence o
         JOIN court_case c
              ON o.court_code = c.court_code AND o.case_no = c.case_no;
