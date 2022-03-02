TRUNCATE courtcaseservicetest.offender_match_group CASCADE;
TRUNCATE courtcaseservicetest.offender_match CASCADE;
TRUNCATE courtcaseservicetest.offender CASCADE;
TRUNCATE courtcaseservicetest.hearing_day CASCADE;
TRUNCATE courtcaseservicetest.hearing CASCADE;
TRUNCATE courtcaseservicetest.court_case CASCADE;
TRUNCATE courtcaseservicetest.court CASCADE;

INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('North Shields', 'B10JQ');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Sheffield', 'B14LO');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Leicester', 'B33HU');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('Aberystwyth', 'B63AD');
INSERT INTO courtcaseservicetest.court (name, court_code) VALUES ('New New York', 'B30NY');
