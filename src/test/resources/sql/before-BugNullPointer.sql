
--- CASE
INSERT INTO courtcaseservicetest.court_case
(id, case_id, case_no, created, created_by, deleted, source_type, urn, last_updated, last_updated_by, "version")
VALUES
(387156, '8a2f1cdc-b66e-4b1f-9fed-03089da8331b', '8a2f1cdc-b66e-4b1f-9fed-03089da8331b', '2023-05-08 11:35:03.415', '(court-case-matcher-2)', false, 'COMMON_PLATFORM', '20CV1244123', '2023-05-08 11:35:03.415', '(court-case-matcher-2)', 0);

---- HEARING
INSERT INTO courtcaseservicetest.hearing
(id, hearing_id, created, created_by, deleted, fk_court_case_id_remove, first_created, hearing_event_type, hearing_type, list_no, fk_court_case_id, last_updated, last_updated_by, "version", fk_hearing_outcome)
VALUES(6927015, 'f57cf283-7a85-4c25-8412-4b73001e0b35', '2023-05-08 11:35:03.415', '(court-case-matcher-2)', false, NULL, '2023-05-08 11:35:03.411', 'CONFIRMED_OR_UPDATED', 'First hearing', NULL, 387156, '2023-05-08 11:35:03.415', '(court-case-matcher-2)', 0, NULL);
INSERT INTO courtcaseservicetest.hearing
(id, hearing_id, created, created_by, deleted, fk_court_case_id_remove, first_created, hearing_event_type, hearing_type, list_no, fk_court_case_id, last_updated, last_updated_by, "version", fk_hearing_outcome)
VALUES(6927016, 'c43c12e3-f8bc-4a07-bbce-63f9034ab360', '2023-05-08 11:35:07.638', '(court-case-matcher-2)', false, NULL, '2023-05-08 11:35:07.631', 'CONFIRMED_OR_UPDATED', 'First hearing', NULL, 387156, '2023-05-08 11:35:07.638', '(court-case-matcher-2)', 0, NULL);
INSERT INTO courtcaseservicetest.hearing
(id, hearing_id, created, created_by, deleted, fk_court_case_id_remove, first_created, hearing_event_type, hearing_type, list_no, fk_court_case_id, last_updated, last_updated_by, "version", fk_hearing_outcome)
VALUES(7108620, '8007724a-ce9a-49f7-b8f5-3b3f177b7138', '2023-06-15 08:49:36.832', '(court-case-matcher-2)', false, NULL, '2023-06-15 08:49:36.822', 'CONFIRMED_OR_UPDATED', 'Bail Application', NULL, 387156, '2023-06-15 08:49:36.832', '(court-case-matcher-2)', 0, NULL);
INSERT INTO courtcaseservicetest.hearing
(id, hearing_id, created, created_by, deleted, fk_court_case_id_remove, first_created, hearing_event_type, hearing_type, list_no, fk_court_case_id, last_updated, last_updated_by, "version", fk_hearing_outcome)
VALUES(7115926, '233efc02-9624-4ba7-abef-a439190d1361', '2023-06-16 11:22:31.974', '(court-case-matcher-2)', false, NULL, '2023-06-16 11:22:31.964', 'CONFIRMED_OR_UPDATED', 'Bail Application', NULL, 387156, '2023-06-16 11:22:31.974', '(court-case-matcher-2)', 0, NULL);

---- HEARING_DAYS
INSERT INTO courtcaseservicetest.hearing_day
(id, fk_hearing_id, hearing_day, hearing_time, court_code, court_room, created, created_by, last_updated, last_updated_by, "version", deleted)
VALUES(8506098, 6927015, '2023-12-25', '10:00:00', 'B20EB', 'Courtroom 03', '2023-06-19 15:33:25.055', '(court-case-matcher-2)', '2023-06-19 15:33:25.055', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_day
(id, fk_hearing_id, hearing_day, hearing_time, court_code, court_room, created, created_by, last_updated, last_updated_by, "version", deleted)
VALUES(7752714, 6927016, '2023-12-25', '10:00:00', 'B20EB', 'Courtroom 03', '2023-05-08 12:04:59.055', '(court-case-matcher-2)', '2023-05-08 12:04:59.055', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_day
(id, fk_hearing_id, hearing_day, hearing_time, court_code, court_room, created, created_by, last_updated, last_updated_by, "version", deleted)
VALUES(8471376, 7108620, '2023-06-16', '09:15:00', '' ||
                                                   '', 'Courtroom 04', '2023-06-16 11:22:31.610', '(court-case-matcher-2)', '2023-06-16 11:22:31.610', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_day
(id, fk_hearing_id, hearing_day, hearing_time, court_code, court_room, created, created_by, last_updated, last_updated_by, "version", deleted)
VALUES(8475165, 7115926, '2023-06-19', '09:00:00', 'C23WR', 'Courtroom 04', '2023-06-16 12:53:50.683', '(court-case-matcher-2)', '2023-06-16 12:53:50.683', '(court-case-matcher-2)', 0, false);


---- OFFENDER
---- this defendant is not a confirmed offender so no offender record

---- DEFENDANT
INSERT INTO courtcaseservicetest.defendant
(id, defendant_name, "type", "name", address, crn, pnc, cro, date_of_birth, sex, nationality_1, nationality_2, created, created_by, manual_update, defendant_id, offender_confirmed, phone_number, person_id, fk_offender_id, last_updated, last_updated_by, "version", deleted, tsv_name)
VALUES(5689837, 'John SMITH', 'PERSON', '{"surname": "SMITH", "forename1": "John"}'::jsonb, '{"line1": "1 Fake Street", "line2": "COVENTRY", "postcode": "CV1 1GN"}'::jsonb, NULL, '2023/0218424R', NULL, '2002-01-24', 'MALE', NULL, NULL, '2023-05-08 11:35:07.641', '(court-case-matcher-2)', false, '14a8d8d3-90db-4422-9e3e-920d7a26e2ad'::uuid, false, NULL, 'd4fa76ee-7cf7-4f39-9e12-05962ee2bc3c'::uuid, NULL, '2023-05-08 11:55:32.977', '(court-case-matcher-2)', 4, false, '''smith'':1 ''john'':2'::tsvector);

INSERT INTO courtcaseservicetest.defendant
(id, defendant_name, "type", "name", address, crn, pnc, cro, date_of_birth, sex, nationality_1, nationality_2, created, created_by, manual_update, defendant_id, offender_confirmed, phone_number, person_id, fk_offender_id, last_updated, last_updated_by, "version", deleted, tsv_name)
VALUES(5689836, 'Dave JONES', 'PERSON', '{"surname": "JONES", "forename1": "Dave"}'::jsonb, '{"line1": "1 Made up Lane", "line2": "COVENTRY", "postcode": "CV1 1JF"}'::jsonb, NULL, '2018/0476888E', NULL, '1998-06-01', 'MALE', NULL, NULL, '2023-05-08 11:35:03.418', '(court-case-matcher-2)', false, 'a25ddee6-68b0-4506-8c64-1f00b3644c61'::uuid, false, NULL, '15d9490f-0627-435a-ba30-341181183ec0'::uuid, NULL, '2023-06-16 11:22:31.978', '(court-case-matcher-2)', 2, false, '''jones'':2 ''dave'':1'::tsvector);


---- HEARING_DEFENDANT
INSERT INTO courtcaseservicetest.hearing_defendant
(id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, "version", deleted)
VALUES(6024024, 6927016, '2023-05-08 11:35:07.641', '(court-case-matcher-2)', '14a8d8d3-90db-4422-9e3e-920d7a26e2ad'::uuid, 5689837, '2023-05-08 11:35:07.641', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_defendant
(id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, "version", deleted)
VALUES(6024055, 6927015, '2023-05-08 11:55:32.851', '(court-case-matcher-2)', '14a8d8d3-90db-4422-9e3e-920d7a26e2ad'::uuid, 5689837, '2023-05-08 11:55:32.856', '(court-case-matcher-2)', 1, false);


INSERT INTO courtcaseservicetest.hearing_defendant
(id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, "version", deleted)
VALUES(6024023, 6927015, '2023-05-08 11:35:03.418', '(court-case-matcher-2)', 'a25ddee6-68b0-4506-8c64-1f00b3644c61'::uuid, 5689836, '2023-05-08 11:35:03.418', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_defendant
(id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, "version", deleted)
VALUES(6209534, 7108620, '2023-06-15 08:49:36.839', '(court-case-matcher-2)', 'a25ddee6-68b0-4506-8c64-1f00b3644c61'::uuid, 5689836, '2023-06-15 08:49:36.839', '(court-case-matcher-2)', 0, false);
INSERT INTO courtcaseservicetest.hearing_defendant
(id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, "version", deleted)
VALUES(6217047, 7115926, '2023-06-16 11:22:31.976', '(court-case-matcher-2)', 'a25ddee6-68b0-4506-8c64-1f00b3644c61'::uuid, 5689836, '2023-06-16 11:22:31.976', '(court-case-matcher-2)', 0, false);



---- HEARING_OUTCOME
INSERT INTO courtcaseservicetest.hearing_outcome
(id, outcome_type, created, created_by, deleted, last_updated, last_updated_by, "version", outcome_date, state, assigned_to, assigned_to_uuid, resulted_date, fk_hearing_id, fk_hearing_defendant_id, legacy)
VALUES(2602, 'NO_OUTCOME', '2023-12-25 15:30:12.651', 'process_unheard_cases_job', false, NULL, NULL, 0, '2023-12-25 15:30:12.651', 'NEW', NULL, NULL, NULL, 6927016, 6024024, false);
INSERT INTO courtcaseservicetest.hearing_outcome
(id, outcome_type, created, created_by, deleted, last_updated, last_updated_by, "version", outcome_date, state, assigned_to, assigned_to_uuid, resulted_date, fk_hearing_id, fk_hearing_defendant_id, legacy)
VALUES(2595, 'NO_OUTCOME', '2023-12-25 15:30:12.651', 'process_unheard_cases_job', false, NULL, NULL, 0, '2023-12-25 15:30:12.651', 'NEW', NULL, NULL, NULL, 6927015, 6024055, true);


