DELETE FROM courtcaseservicetest.offence WHERE true;
DELETE FROM courtcaseservicetest.court_case WHERE true;
DELETE FROM courtcaseservicetest.court WHERE true;

INSERT INTO courtcaseservicetest.court (id, name, court_code) VALUES (4444443, 'Sheffield Magistrates Court Test', 'SHF');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach, defendant_name, defendant_address) VALUES (5555555, 1600028913, 'SHF', 1, '2019-12-14 09:00', 'Previously known','{"inf": "POL01", "h_id": 1246000, "def_name": "JTEST", "def_addr": {"line3": "a3", "line2": "a2", "line1": "a1"}, "type": "C", "caseno": 1111111111, "def_dob": "01/01/1998", "valid": "Y", "cseq": 1, "listno": "1st", "offences": {"offence": [{"as": "Contrary to section 1(1) and 7 of the Theft Act 1968.", "code": "TH68010", "oseq": 1, "co_id": 1142222, "maxpen": "EW: 6M &/or Ultd Fine", "sum": "On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.", "title": "Theft from a shop"}, {"as": "Contrary to section 1(1) and 7 of the Theft Act 1968.", "code": "TH68010", "oseq": 2, "co_id": 1142222, "maxpen": "EW: 6M &/or Ultd Fine", "sum": "On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.", "title": "Theft from a shop"}]}, "def_age": 18, "c_id": 3333333, "def_type": "P", "def_sex": "M"}', '2019-12-14 09:00', '2010-01-01', true, true, 'JTEST', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}');

INSERT INTO OFFENCE (
	CASE_ID,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        5555555,
        'Theft from a shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1
	);

INSERT INTO OFFENCE (
	CASE_ID,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        5555555,
        'Theft from a different shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1
	);

-- See CourtCaseControllerTest.shouldGetCaseListWhenCasesExist()
-- These records are used to test edge cases when returning court case list for a given date (midnight to 1 second before midnight the next day)
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, data, probation_status) VALUES (5555556, 1600028914, 'SHF', 1,
'2019-12-14 00:00', '{}', 'No record');
INSERT INTO courtcaseservicetest.court_case (case_id, case_no, court_code, court_room, session_start_time, data, probation_status) VALUES (5555557, 1600028915, 'SHF', 1,
'2019-12-14 23:59:59', '{}', 'No record');



