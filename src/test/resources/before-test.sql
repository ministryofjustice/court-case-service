TRUNCATE courtcaseservicetest.offender_match_group_offender_matches CASCADE;
TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.offence CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;

INSERT INTO courtcaseservicetest.court (id, name, court_code) VALUES (4444443, 'Sheffield Magistrates Court Test', 'SHF');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address, crn, pnc, cro, list_no, defendant_dob, defendant_sex, nationality_1, nationality_2) VALUES (5555555, 1600028913, 'SHF', 1, '2019-12-14 09:00', 'Previously known', '2019-12-14 09:00', '2010-01-01', true, true, 'JTEST', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'X320741', 'A/1234560BA', '311462/13E', '3rd', '1958-10-10', 'M', 'British', 'Polish' );

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
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn) VALUES (5555556, 1600028914, 'SHF', 1,
'2019-12-14 00:00', 'No record', 'X320741');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, crn) VALUES (5555557, 1600028915, 'SHF', 1,
'2019-12-14 23:59:59', 'No record', 'X320741');



