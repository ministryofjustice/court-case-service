INSERT INTO courtcaseservicetest.OFFENDER (id, crn, pnc, cro, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-1000001, 'X320741', 'PNCINT007', 'CROINT007', 'CURRENT', '2010-01-01', true, true, true, true, 'court-case-history.sql');

INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type, urn) VALUES (-1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', 1600028913, now(), 'LIBRA', 'URN008');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created) VALUES (-1700028900, -1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', now());
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created) VALUES (-1700028899, -1700028900, '1f93aa0a-7e46-4885-a1cb-f25a4be33a00', now());
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created) VALUES (-1700028898, -1700028900, '2aa6f5e0-f842-4939-bc6a-01346abc09e7', now());

INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no)
VALUES (-1000000, -1700028900, 'B10JQ', 1, '2019-12-14', '09:00', '3rd');
INSERT INTO courtcaseservicetest.DEFENDANT(id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, pnc, cro, sex, nationality_1, nationality_2, phone_number)
VALUES (-1000000, '40db17d6-04db-11ec-b2d8-0242ac130002', 'Mr Johnny BALL', '{"title": "Mr", "surname": "BALL", "forename1": "Johnny", "forename2": "John", "forename3": "Jon"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X320741', 'A/1234560BA', '311462/13E', 'MALE', 'British', 'Polish', '{"home": "07000000013", "mobile": "07000000007", "work": "07000000015"}');

INSERT INTO courtcaseservicetest.HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID)
VALUES (-1000000, -1700028900, '40db17d6-04db-11ec-b2d8-0242ac130002');
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
