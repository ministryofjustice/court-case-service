INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by, version)
VALUES (-100, 'X258291', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test', 1);

INSERT INTO OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-99, 'X12345', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test');

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-184, 'fe657c3a-b674-4e17-8772-7281c99e4f9f', '1600028888', '2022-03-23 17:59:59.000', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-184, -184, 'fe657c3a-b674-4e17-8772-7281c99e4f9f', '2022-03-23 17:59:59.000');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-184, -184, 'B33HU', 1, NOW()::date - interval '5 days', '13:00', '2022-03-23 17:59:59.000');
INSERT INTO DEFENDANT (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, FK_OFFENDER_ID, sex, created, cpr_uuid)
VALUES (-184, '0048297a-fd9c-4c96-8c03-8122b802a54d', 'c88fd38c-4b9e-11ed-bdc3-0242ac120002', 'Mr Ferris Middle BUELLER', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris", "forename2": "Antimony"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X258291', -100, 'MALE', '2022-03-23 17:59:59.000', 'cd33edce-5948-4592-a4ac-b5eb48d01209');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-184, -184, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d', -184);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-198, -184, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 17:59:59.000');

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-154, '702dcb38-f3ec-446a-84e5-b6bceb6bcfd0', '1600028888', '2022-01-23 17:59:59.000', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-154, -154, '440dd779-8b0e-4012-90d5-2e2ee1189cd1', '2022-01-23 17:59:59.000');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-154, -154, 'B33HU', 1, NOW()::date - interval '5 days', '10:00', '2022-01-23 17:59:59.000');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-155, -154, 'B14LO', 1, NOW()::date - interval '2 days', '09:00', '2022-01-23 17:59:59.000');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-156, -154, 'B33HU', 1, NOW()::date + interval '10 days', '09:00', '2022-01-23 17:59:59.000');
INSERT INTO DEFENDANT (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, FK_OFFENDER_ID, sex, created, cpr_uuid)
VALUES (-154, '8acf5a7a-0e0b-49e5-941e-943ab354a15f', '8acf5a7a-0e0b-49e5-941e-943ab354a15f', 'Mr Ferris Middle Biller', '{"title": "Mr", "surname": "Biller", "forename1": "Ferris", "forename2": "Antimony"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X258291', -100, 'MALE', '2022-01-23 17:59:59.000', 'dd33edce-5948-4592-a4ac-b5eb48d01209');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-154, -154, '2022-01-23 16:59:59.000', '8acf5a7a-0e0b-49e5-941e-943ab354a15f', -154);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-154, -154, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-01-23 17:59:59.000');
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-155, -154, 'Theft from a hospital', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-01-23 17:59:59.000');

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-164, 'd88cfb92-915c-43ea-90cd-a13b3e4622aa', '1600028888', '2022-02-23 17:59:59.000', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-164, -164, '42936880-43c6-44b3-a009-8c4040ed1832', '2022-02-23 17:59:59.000');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-164, -164, 'B63AD', 1, '2022-1-25', '09:00', '2022-02-23 17:59:59.000');
INSERT INTO DEFENDANT (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, FK_OFFENDER_ID, sex, created, cpr_uuid)
VALUES (-164, '9f8c4a3f-68d1-43a1-b743-163edca3bc68', '9f8c4a3f-68d1-43a1-b743-163edca3bc68', 'Mr John Middle Bloggs', '{"title": "Mr", "surname": "Bloggs", "forename1": "John", "forename2": "Antimony"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X12345', -99, 'MALE', '2022-02-23 17:59:59.000', 'ad33edce-5948-4592-a4ac-b5eb48d01209');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-164, -164, '2022-02-23 16:59:59.000', '9f8c4a3f-68d1-43a1-b743-163edca3bc68', -164);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-164, -164, 'Theft from a hospital', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-02-23 17:59:59.000');

