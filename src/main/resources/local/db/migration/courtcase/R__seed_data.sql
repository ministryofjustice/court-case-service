INSERT INTO courtcaseservice.court_case (id, case_id, case_no, created, created_by, deleted, source_type, urn, last_updated, last_updated_by, version)
VALUES (2, 'fab9537e-2c73-4839-938e-5d9eb3230d9d', '23087575169151030123', '2023-08-08 16:58:57.567054', '(court-case-matcher-7)', false, 'LIBRA', '12GD3434519', '2023-08-08 16:58:57.567054', '(court-case-matcher-7)', 0);

INSERT INTO courtcaseservice.hearing (id, hearing_id, created, created_by, deleted, fk_court_case_id_remove, first_created, hearing_event_type, hearing_type, list_no, fk_court_case_id, last_updated, last_updated_by, version, fk_hearing_outcome)
VALUES (1, 'f5f0fb09-1419-442f-8f53-b85437410655', '2022-12-08 11:20:46.830397', '(court-case-matcher)', false, NULL, '2022-12-08 11:20:46.799473', 'CONFIRMED_OR_UPDATED', 'Sentence', NULL, 2, '2022-12-08 11:20:46.830397', '(court-case-matcher)', 0, 1);

INSERT INTO courtcaseservice.offender (id, crn, previously_known_termination_date, probation_status, suspended_sentence_order, breach, pre_sentence_activity, awaiting_psr, created, created_by, last_updated, last_updated_by, version, deleted, pnc, cro)
VALUES (1, 'X757296', NULL, 'NOT_SENTENCED', false, false, false, true, '2024-03-25 17:56:02.667054', NULL, '2024-03-25 17:56:02.681225', NULL, 1, false, '1975/8516064G', '');

INSERT INTO courtcaseservice.defendant (id, defendant_name, type, name, address, crn, pnc, cro, date_of_birth, sex, nationality_1, nationality_2, created, created_by, manual_update, defendant_id, offender_confirmed, phone_number, person_id, fk_offender_id, last_updated, last_updated_by, version, deleted, tsv_name)
VALUES (1, 'Mrs Lagertha Lothbrok', 'PERSON', '{"title": "Mrs", "surname": "Lothbrok", "forename1": "Lagertha"}', '{"line1": "39 The Street", "line2": "Newtown", "postcode": "NT4 6YH"}', 'X757296', '1975/8516064G', NULL, '1986-11-28', 'FEMALE', NULL, NULL, '2023-08-03 09:00:19.706358', '(court-case-matcher-7)', false, 'b6815b01-f2a5-459b-8ca8-955a6825a5fa', false, NULL, '64209971-736a-41c1-a5d8-86e2ec212001', 1, '2023-08-03 09:00:19.706358', '(court-case-matcher-7)', 0, false, '''lagertha'':2 ''lothbrok'':3 ''mrs'':1');

INSERT INTO courtcaseservice.hearing_defendant (id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id, last_updated, last_updated_by, version, deleted)
VALUES (1, 1, '2022-04-07 09:45:48.915858', '(court-case-matcher)', 'b6815b01-f2a5-459b-8ca8-955a6825a5fa', 1, NULL, NULL, 0, false);

INSERT INTO courtcaseservice.hearing_outcome (id, outcome_type, created, created_by, deleted, last_updated, last_updated_by, version, outcome_date, state, assigned_to, assigned_to_uuid, resulted_date, fk_hearing_id, fk_hearing_defendant_id, legacy)
VALUES (1, 'NO_OUTCOME', '2023-12-15 15:44:27.295798', 'process_unheard_cases_job', false, '2024-02-09 14:43:02.066624', 'PATRICK(prepare-a-case-for-court-1)', 2, '2023-12-15 15:44:27.295798', 'RESULTED', 'Patrick Graham', 'b20ebf72-7658-4027-96a2-650a424cfeef', '2024-02-09 14:43:02.061125', 1, 1, false);


INSERT INTO courtcaseservice.offence (id, fk_hearing_defendant_id, summary, title, sequence, act, created, created_by, list_no, last_updated, last_updated_by, version, deleted, offence_code, short_term_custody_predictor_score, plea_id, verdict_id)
VALUES (1, 1, 'On 01/01/2016 at Town, made up stupid offences to amuse self.', 'Making up test data last thing on a Friday', 1, 'Contrary to section 1(1) and 7 of the Poor Use of Company Time Act 1968.', '2022-05-24 11:31:33.980674', '(court-case-matcher)', NULL, NULL, NULL, 0, false, NULL, NULL, NULL, NULL);

