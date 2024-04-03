INSERT INTO
    courtcaseservicetest.OFFENDER
    (id, crn, pnc, cro, probation_status, previously_known_termination_date, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, version, created_by)
VALUES (-1000001, 'P133818', null, null, 'PREVIOUSLY_KNOWN', '2020-04-08', false, false, true, false, 3, 'bug-null-pointer.sql');

INSERT INTO
    courtcaseservicetest.court_case
    (id, case_id, case_no, created, source_type, urn, created_by)
VALUES (-1700028600, '8a2f1cdc-b66e-4b1f-9fed-03089da8331b', '8a2f1cdc-b66e-4b1f-9fed-03089da8331b', TO_TIMESTAMP('2023-05-08 11:35:03', 'YYYY-MM-DD HH24:MI:SS'), 'COMMON_PLATFORM', '20CV1244123', 'bug-null-pointer.sql');

INSERT INTO
    courtcaseservicetest.hearing
    (id, fk_court_case_id, hearing_id, hearing_event_type, hearing_type, created, fk_hearing_outcome, created_by)
VALUES (-1700028700, -1700028600, 'b41ff816-5ff6-418a-a1f5-30a587830c03', 'CONFIRMED_OR_UPDATED', 'First hearing', TO_TIMESTAMP('2023-05-16 07:58:05', 'YYYY-MM-DD HH24:MI:SS'), null, 'bug-null-pointer.sql');

INSERT INTO
    courtcaseservicetest.hearing
(id, fk_court_case_id, hearing_id, hearing_event_type, hearing_type, created, fk_hearing_outcome, created_by)
VALUES (-1700028701, -1700028600, '15cd65e6-eed1-4ecc-bd6b-37159d703733', 'CONFIRMED_OR_UPDATED', 'Trial', TO_TIMESTAMP('2023-07-05 15:34:29', 'YYYY-MM-DD HH24:MI:SS'), null, 'bug-null-pointer.sql');

INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created_by)
VALUES (-1000000, -1700028700, 'B20EB', 'Courtroom 07', '2023-07-05', '12:30', 'bug-null-pointer.sql');
INSERT INTO courtcaseservicetest.HEARING_DAY (id, fk_hearing_id, court_code, court_room, hearing_day, hearing_time, created_by)
VALUES (-1000002, -1700028701, 'B20EB', 'Courtroom 07', '2023-12-21', '10:00', 'bug-null-pointer.sql');

INSERT INTO
    courtcaseservicetest.DEFENDANT
    (id, DEFENDANT_ID, PERSON_ID, defendant_name, name, address, type, date_of_birth, crn, fk_offender_id, pnc, cro, sex, nationality_1, nationality_2, phone_number)
VALUES (-2000000, 'f6e2482a-8230-4b4c-ab7d-716bd4000a5b', 'd12f694a-b56c-4cd5-93f8-9c44b5768162', 'Mark BERRY', '{"surname": "BERRY", "forename1": "Mark"}', '{"line1": "96", "line2": "ALPHA HOUSE", "line3": "BARRAS GREEN", "line4": "COVENTRY", "postcode": "CV2 4PL"}', 'PERSON', '1978-12-21', null, -1000001, '1991/0484991Y', '154100/91T', 'MALE', null, null, '{"mobile": "07762350106"}');

INSERT INTO courtcaseservicetest.HEARING_DEFENDANT(id, fk_hearing_id, DEFENDANT_ID, FK_DEFENDANT_ID)
VALUES (-2000000, -1700028700, 'f6e2482a-8230-4b4c-ab7d-716bd4000a5b', -2000000);

-- ID, FK_HEARING_DEFENDANT_ID, TITLE, SUMMARY, ACT, SEQUENCE, LIST_NO

INSERT INTO courtcaseservicetest.offence
(id, fk_hearing_defendant_id, summary, title, "sequence", act, created_by, list_no)
VALUES(-3000000, -2000000, 'On or in 5 May 2023 at Coventry assaulted Ben Ianson by beating him', 'Assault by beating', 1, 'Contrary to section 39 of the Criminal Justice Act 1988.', 'bug-null-pointer.sql', 1);

-- id, outcome_type, outcome_date, state, created, created_by, assigned_to, assigned_to_uuid, fk_hearing_id, fk_hearing_defendant_id

INSERT INTO courtcaseservicetest.hearing_outcome
(id, outcome_type, created, created_by, deleted, last_updated, last_updated_by, "version", outcome_date, state, assigned_to, assigned_to_uuid, resulted_date, fk_hearing_id, fk_hearing_defendant_id, legacy)
VALUES(2579, 'NO_OUTCOME', TO_TIMESTAMP('2023-12-21 15:30:08', 'YYYY-MM-DD HH24:MI:SS'), 'process_unheard_cases_job', false, NULL, NULL, 0, TO_TIMESTAMP('2023-12-21 15:30:08', 'YYYY-MM-DD HH24:MI:SS'), 'NEW', NULL, NULL, NULL, -1700028700, -2000000, false);
