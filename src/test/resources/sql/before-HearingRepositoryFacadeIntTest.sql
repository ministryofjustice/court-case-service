-- Initial creation of Ferris Bueller
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-199, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-199, -199, '5564cbfd-3d53-4f36-9508-437416b08738', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-199, -199, 'B10JQ', 1, '2019-12-14', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-199, '0048297a-fd9c-4c96-8c03-8122b802a54d', 'Mr Ferris BUELLER', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-199, -199, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-199, -199, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');

INSERT INTO courtcaseservicetest.case_comments(id, case_id, comment, "author", created, created_by, created_by_uuid) VALUES (-1700028900, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', 'PSR in progress', 'Author One', now(), 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');
INSERT INTO courtcaseservicetest.case_comments(id, case_id, comment, "author", created, deleted, created_by, created_by_uuid) VALUES (-1700028901, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', 'PSR completed', 'Author One', now(), true, 'before-test.sql', 'fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81');

-- Update to Ferris Bueller
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-198, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2022-03-23 17:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-198, -198, '5564cbfd-3d53-4f36-9508-437416b08738', '2022-03-23 17:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-198, -198, 'B33HU', 1, '2022-3-25', '09:00', '3rd', '2022-03-23 17:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-198, '0048297a-fd9c-4c96-8c03-8122b802a54d', 'Mr Ferris BUELLER', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris", "forename2": "Antimony"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X25829', 'MALE', '2022-03-23 17:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-198, -198, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-198, -198, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 17:59:59.000');

INSERT INTO courtcaseservicetest.OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by, version)
VALUES (-100, 'X25829', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test', 1);

-- Initial creation of Royston Vasey - Error case - offender referenced by CRN does not exist
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-197, '2d85ba01-676e-409f-9336-815c4ce90f04', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-197, -197, 'db63e9b5-6263-4235-9c4e-a99e200ae33e', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-197, -197, 'B10JQ', 1, '2019-12-14', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-197, 'd1a4f1b7-b153-4740-b68a-2b84feff6996', 'Mr Royston Vasey', '{"title": "Mr", "surname": "Vasey", "forename1": "Royston"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'NOT_EXTANT', 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-197, -197, '2022-03-23 16:59:59.000', 'd1a4f1b7-b153-4740-b68a-2b84feff6996');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-197, -197, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');


-- Case list

-- Updated hearing - hearing moved into this court
-- Creation
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-196, '3a3f5334-34c7-4caa-9b7a-9495663ea2da', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-196, -196, '1bfff8b7-fbc6-413f-8545-8299c26f75bd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-196, -196, 'B10JQ', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-196, '62d57ee8-a7a6-4b36-857d-8ced9e2aac9b', 'Mr Hearing Moved In', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-196, -196, '2022-03-23 16:59:59.000', '62d57ee8-a7a6-4b36-857d-8ced9e2aac9b');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-196, -196, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-195, '3a3f5334-34c7-4caa-9b7a-9495663ea2da', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-195, -195, '1bfff8b7-fbc6-413f-8545-8299c26f75bd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-195, -195, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-195, '62d57ee8-a7a6-4b36-857d-8ced9e2aac9b', 'Mr Hearing Moved In', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-195, -195, '2022-03-23 16:59:59.000', '62d57ee8-a7a6-4b36-857d-8ced9e2aac9b');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-195, -195, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');

-- Updated hearing - hearing moved out of this court (SHOULD NOT SHOW IN LIST)
-- Creation
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-194, '3d54c880-c44e-4331-b310-02350bebc1bf', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-194, -194, 'e7fa5afa-55ed-4029-9414-614a406d4938', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-194, -194, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-194, '58e61be2-d535-4d8c-a121-30e104fffb7d', 'Mr Hearing Moved Out', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-194, -194, '2022-03-23 16:59:59.000', '58e61be2-d535-4d8c-a121-30e104fffb7d');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-194, -194, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-193, '3d54c880-c44e-4331-b310-02350bebc1bf', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-193, -193, 'e7fa5afa-55ed-4029-9414-614a406d4938', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-193, -193, 'B63AD', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-193, '58e61be2-d535-4d8c-a121-30e104fffb7d', 'Mr Hearing Moved Out', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-193, -193, '2022-03-23 16:59:59.000', '58e61be2-d535-4d8c-a121-30e104fffb7d');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-193, -193, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');

-- Updated hearing - hearing moved to this day
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-192, '6ce742e2-6539-4380-a9b6-2241a9854ae8', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-192, -192, '7c1ccb0e-399a-4a28-a866-f52c139735f6', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-192, -192, 'B14LO', 1, '2022-02-01', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-192, 'bfdf6193-61c1-44df-b496-96d2e1c1b779', 'Mr Hearing Moved To This Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-192, -192, '2022-03-23 16:59:59.000', 'bfdf6193-61c1-44df-b496-96d2e1c1b779');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-192, -192, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-191, '6ce742e2-6539-4380-a9b6-2241a9854ae8', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-191, -191, '7c1ccb0e-399a-4a28-a866-f52c139735f6', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-191, -191, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-191, 'bfdf6193-61c1-44df-b496-96d2e1c1b779', 'Mr Hearing Moved To This Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-191, -191, '2022-03-23 16:59:59.000', 'bfdf6193-61c1-44df-b496-96d2e1c1b779');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-191, -191, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');

-- Updated hearing - hearing moved to another day (SHOULD NOT SHOW IN LIST)
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-190, '608c4229-2ca9-4e9c-a18e-62023bd1ef49', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-190, -190, 'c27c0f42-7287-43f7-96dd-47c402358842', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-190, -190, 'B14LO', 1, '2022-02-01', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-190, 'bfa32fc4-bba0-4197-b795-3e1a85a8a8da', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-190, -190, '2022-03-23 16:59:59.000', 'bfa32fc4-bba0-4197-b795-3e1a85a8a8da');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-190, -190, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-189, '608c4229-2ca9-4e9c-a18e-62023bd1ef49', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-189, -189, 'c27c0f42-7287-43f7-96dd-47c402358842', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-189, -189, 'B14LO', 1, '2022-04-02', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-189, 'bfa32fc4-bba0-4197-b795-3e1a85a8a8da', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-189, -189, '2022-03-23 16:59:59.000', 'bfa32fc4-bba0-4197-b795-3e1a85a8a8da');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-189, -189, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');

-- Updated hearing - defendant added
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-188, 'f56f87c3-de79-4deb-b23d-b99cd91ba21d', '1600028913', '2022-03-23 16:59:59.000', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-188, -188, 'c12be6d5-2d6e-432b-a147-94c49171ef1d', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-188, -188, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-188, '50110880-1573-4fc9-8b16-8bb85413d37e', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.000');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-188, -188, '2022-03-23 16:59:59.000', '50110880-1573-4fc9-8b16-8bb85413d37e');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-188, -188, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.000');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-187, 'f56f87c3-de79-4deb-b23d-b99cd91ba21d', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-187, -187, 'c12be6d5-2d6e-432b-a147-94c49171ef1d', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-187, -187, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-187, '50110880-1573-4fc9-8b16-8bb85413d37e', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-187, -187, '2022-03-23 16:59:59.000', '50110880-1573-4fc9-8b16-8bb85413d37e');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-10187, 'b564248c-d97a-490e-914b-3c04ada4a99e', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X12345', 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-10187, -187, '2022-03-23 16:59:59.000', 'b564248c-d97a-490e-914b-3c04ada4a99e');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-187, -187, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');

INSERT INTO courtcaseservicetest.OFFENDER (id, crn, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created_by)
VALUES (-99, 'X12345', 'CURRENT', '2010-01-01', true, true, true, true, 'before-test');

-- Updated hearing - defendant removed
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-186, 'fc696266-3ed9-451a-bc85-e1d27186e649', '1600028913', '2022-03-23 16:59:59.001', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-186, -186, '961f6b9d-ae7e-4998-9d5d-4f56ceadce99', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-186, -186, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-186, '4e26fdb6-aa78-4cc0-a6b4-710665bf4c44', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-186, -186, '2022-03-23 16:59:59.000', '4e26fdb6-aa78-4cc0-a6b4-710665bf4c44');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-10186, '43f66525-630b-4ee8-ba04-bae07a0c190a', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X12345', 'MALE', '2022-03-23 16:59:59.001');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-10186, -186, '2022-03-23 16:59:59.000', '43f66525-630b-4ee8-ba04-bae07a0c190a');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-186, -186, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.001');
-- Update
INSERT INTO courtcaseservicetest.court_case (id, case_id, case_no, created, source_type)
VALUES (-185, 'fc696266-3ed9-451a-bc85-e1d27186e649', '1600028913', '2022-03-23 16:59:59.002', 'COMMON_PLATFORM');
INSERT INTO courtcaseservicetest.hearing (id, fk_court_case_id, hearing_id, created)
VALUES (-185, -185, '961f6b9d-ae7e-4998-9d5d-4f56ceadce99', '2022-03-23 16:59:59.002');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, list_no, created)
VALUES (-185, -185, 'B14LO', 1, '2022-03-23', '09:00', '3rd', '2022-03-23 16:59:59.002');
INSERT INTO courtcaseservicetest.DEFENDANT (id, DEFENDANT_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-185, '4e26fdb6-aa78-4cc0-a6b4-710665bf4c44', 'Mr Hearing Moved To Another Day', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', null, 'MALE', '2022-03-23 16:59:59.002');
INSERT INTO courtcaseservicetest.HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id)
VALUES (-185, -185, '2022-03-23 16:59:59.000', '4e26fdb6-aa78-4cc0-a6b4-710665bf4c44');
INSERT INTO courtcaseservicetest.OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-185, -185, 'Theft from a shop', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 16:59:59.002');
