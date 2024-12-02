-- Ferris Bueller
INSERT INTO DEFENDANT (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, sex, created)
VALUES (-999, '0048297a-fd9c-4c96-8c03-8122b802a54d', '732cce04-4b9e-11ed-bdc3-0242ac120002', 'Mr Ferris BUELLER', '{"title": "Mr", "surname": "BUELLER", "forename1": "Ferris", "forename2": "Antimony"}', '{"line1": "27", "line2": "Elm Place", "postcode": "ad21 5dr", "line3": "Bangor", "line4": null, "line5": null}', 'PERSON', '1958-10-10', 'X25829', 'MALE', '2022-03-23 17:59:59.000');

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-197, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2024-11-23 17:59:58.500', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-197, -197, '5564cbfd-3d53-4f36-9508-437416b08738', '2024-11-23 17:59:58.500', '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-197, -197, 'B33HU', 1, '2022-3-25', '09:00', '2024-11-23 17:59:59.000');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-197, -197, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d', -999);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-197, -197, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 17:59:59.000');

-- Insert duplicate data x 1
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-198, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2024-11-23 17:59:59.000', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-198, -198, '5564cbfd-3d53-4f36-9508-437416b08738', '2024-11-23 17:59:59.000', '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-198, -198, 'B33HU', 1, '2022-3-25', '09:00', '2024-11-23 17:59:59.000');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-198, -198, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d', -999);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-198, -198, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 17:59:59.000');

-- Insert duplicate data x2
-- hearing and court_case created date is 1 second before original
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-199, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2024-11-23 17:59:59.100', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-199, -199, '5564cbfd-3d53-4f36-9508-437416b08738', '2024-11-23 17:59:59.100', '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-199, -199, 'B33HU', 1, '2022-3-25', '09:00', '2024-11-23 17:59:59.100');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-199, -199, '2022-03-23 16:59:59.000', '0048297a-fd9c-4c96-8c03-8122b802a54d', -999);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-199, -199, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2022-03-23 17:59:59.000');

-- Insert duplicate data x3
-- The data is created before the p2 incident start date
INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (-200, '727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b', '1600028888', '2024-11-21 11:59:59', 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, list_no)
VALUES (-200, -200, '5564cbfd-3d53-4f36-9508-437416b08738', '2024-11-21 11:59:59', '3rd');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (-200, -200, 'B33HU', 1, '2022-3-25', '09:00', '2024-11-21 11:59:59');
INSERT INTO HEARING_DEFENDANT (id, fk_hearing_id, created, defendant_id, FK_DEFENDANT_ID)
VALUES (-200, -200, '2024-11-21 11:59:59', '0048297a-fd9c-4c96-8c03-8122b802a54d', -999);
INSERT INTO OFFENCE (ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, CREATED)
VALUES (-200, -200, 'Theft from a garage', 'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.', 'Contrary to section 1(1) and 7 of the Theft Act 1968.', 1, '2024-11-21 11:59:59');