--Test data for domain event listener OffenderDomainEventListenerIntTest
INSERT INTO court (name, court_code) VALUES ('Old New York', 'C10JQ');

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (4000022, '3a43f58e-950d-4e54-97d0-8fb272f7e299', 4000022, now(), 'COMMON_PLATFORM');
INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, first_created)
VALUES (4000022, 4000022, 'f0492ab5-e1fd-4d7a-9ba6-ec3b058ec017', now(), '2020-10-01 16:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (4000022, 4000022, 'C10JQ', 2, '2019-12-14', '13:00:00', now());

INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name, date_of_birth, pnc, type, sex)
VALUES (4000022, '43e2efe3-77df-40d1-8c11-23a719ca25e8', 'f0250ed4-1487-4a14-b65f-2953de3b33e8', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', '1939-10-10','PN/1234560BA', 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (4000022, 4000022, '43e2efe3-77df-40d1-8c11-23a719ca25e8', 4000022);
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name,date_of_birth, pnc, type, sex)
VALUES (4000023, '02da0ef3-1d8a-4f7d-9582-ceb30a579176', '30f252a3-58c4-44d2-93c7-446f3dba9824', 'Mr David BOWIE', '{"title": "Mr", "surname": "BOWIE", "forename1": "David"}', '1939-10-10', 'PN/1234560XX', 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (4000023, 4000022, '02da0ef3-1d8a-4f7d-9582-ceb30a579176', 4000023);

INSERT INTO court_case (id, case_id, case_no, created, source_type)
VALUES (4000024, '9f309ac5-9589-41b7-87fb-ca2c37a6d4e9', 4000024, now(), 'COMMON_PLATFORM');

INSERT INTO hearing (id, fk_court_case_id, hearing_id, created, first_created)
VALUES (4000024, 4000024, '8fbd1192-221f-4fa3-9ddd-e192acf34d45', now(), '2020-10-01 16:59:59');
INSERT INTO HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created)
VALUES (4000024, 4000024, 'C10JQ', 2, '2019-12-14', '13:00:00', now());
INSERT INTO DEFENDANT(id, DEFENDANT_ID, PERSON_ID, defendant_name, name,date_of_birth, pnc, type, sex)
VALUES (4000024, '5627f774-4599-4c8b-8de0-87d2f55fe173', 'cf437aa6-b45e-4504-9aa3-a41cc3336608', 'Mr David BOWIE', '{"title": "Mr", "surname": "bowie", "forename1": "david"}', '1939-10-10', 'PN/1234560XX', 'PERSON', 'MALE');
INSERT INTO HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (4000024, 4000024, '5627f774-4599-4c8b-8de0-87d2f55fe173', 4000024);
-- End