TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.hearing_day CASCADE;
TRUNCATE courtcaseservicetest.hearing CASCADE;
TRUNCATE courtcaseservicetest.hearing_defendant CASCADE;
TRUNCATE courtcaseservicetest.defendant CASCADE;
TRUNCATE courtcaseservicetest.offender CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;
TRUNCATE courtcaseservicetest.case_comments CASCADE;
TRUNCATE courtcaseservicetest.hearing_outcome CASCADE;
TRUNCATE courtcaseservicetest.hearing_notes CASCADE;

TRUNCATE courtcaseservicetest.hearing_day_aud CASCADE;
TRUNCATE courtcaseservicetest.offence_aud CASCADE;
TRUNCATE courtcaseservicetest.hearing_defendant_aud CASCADE;
TRUNCATE courtcaseservicetest.defendant_aud CASCADE;
TRUNCATE courtcaseservicetest.hearing_aud CASCADE;
TRUNCATE courtcaseservicetest.court_case_aud CASCADE;

INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('North Shields', 'B10JQ');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Sheffield', 'B14LO');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Leicester', 'B33HU');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Aberystwyth', 'B63AD');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('New New York', 'B30NY');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Coventry', 'B20EB');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Warwick Crown Court sitting at Leamington Spa', 'C23WR');
