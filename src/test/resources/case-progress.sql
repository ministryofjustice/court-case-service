INSERT INTO courtcaseservicetest.OFFENDER (id, crn, pnc, cro, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000001, 'X320741', 'PNCINT007', 'CROINT007', 'CURRENT', '2010-01-01', true, true, true, true, 'court-case-history.sql');


INSERT INTO courtcaseservicetest.court_case (id, case_id, created, source_type, urn) VALUES (-1700028600, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', now(), 'COMMON_PLATFORM', 'URN008');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, hearing_type, created) VALUES (-1700028600, -1700028600, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'Sentence', TO_TIMESTAMP('2019-12-14 9:00:00', 'YYYY-MM-DD HH:MI:SS'));
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, hearing_type, created) VALUES (-1700028899, -1700028600, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'Sentence', TO_TIMESTAMP('2019-11-14 9:00:00', 'YYYY-MM-DD HH:MI:SS'));
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, hearing_type, created) VALUES (-1700028898, -1700028600, '2aa6f5e0-f842-4939-bc6a-01346abc09e7', 'Hearing', TO_TIMESTAMP('2019-10-14 9:00:00', 'YYYY-MM-DD HH:MI:SS'));

INSERT INTO courtcaseservicetest.hearing_notes(id, hearing_id, note, "author", created, created_by, created_by_uuid)
VALUES (-1700028800, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'Judge heard', 'Author One', now(), 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');
INSERT INTO courtcaseservicetest.hearing_notes(id, hearing_id, note, "author", created, deleted, created_by, created_by_uuid)
VALUES (-1700028801, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'Judge sentenced', 'Author two', now(), true, 'before-test.sql', '389fd9cf-390e-469a-b4cf-6c12024c4cae');
INSERT INTO courtcaseservicetest.hearing_notes(id, hearing_id, note, "author", created, deleted, created_by, created_by_uuid)
VALUES (-1700028802, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 'Judge sentenced', 'Author three', now(), false, 'before-test.sql', '389fd9cf-390e-469a-b4cf-6c12024c4cae');

INSERT INTO courtcaseservicetest.hearing_notes(id, hearing_id, note, "author", created, created_by, created_by_uuid)
VALUES (-1700028803, '2aa6f5e0-f842-4939-bc6a-01346abc09e7', 'Judge requested PSR', 'Author Three', now(), 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');

INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1000000, -1700028600, 'B10JQ', 2, '2019-11-14', '09:00');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1000002, -1700028600, 'B10JQ', 1, '2019-12-14', '09:00');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time)
VALUES (-1000001, -1700028898, 'B33HU', 2, '2019-10-14', '09:00');
INSERT INTO courtcaseservicetest.DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, phone_number)
VALUES (-1000000, '40db17d6-04db-11ec-b2d8-0242ac130002', 'c1a19768-4bad-11ed-bdc3-0242ac120002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X320741', 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', '{"home": "07000000013", "mobile": "07000000007", "work": "07000000015"}');

INSERT INTO courtcaseservicetest.HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID)
VALUES (-1000000, -1700028600, '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID)
VALUES (-1000100, -1700028899, '40db17d6-04db-11ec-b2d8-0242ac130002');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID)
VALUES (-1000110, -1700028898, '40db17d6-04db-11ec-b2d8-0242ac130002');

INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, LIST_NO)
VALUES (-1000000, -1000000, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, 10);
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000001, -1000000, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, LIST_NO)
VALUES (-1000100, -1000110, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, 10);
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000101, -1000110, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);

INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE)
VALUES (-1000102, -1000100, 'Theft from a different shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 2);
