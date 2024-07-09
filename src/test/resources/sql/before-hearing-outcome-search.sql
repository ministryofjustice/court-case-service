INSERT INTO court_case (id, case_id,case_no,created,created_by,deleted,source_type,urn,last_updated,last_updated_by,"version")
VALUES
    (6252, '1afb8c07-207c-412d-96ff-1ac5ee7f0b3f','1afb8c07-207c-412d-96ff-1ac5ee7f0b3f','2023-05-11 09:25:45.646','(court-case-matcher-7)',false,'COMMON_PLATFORM','25GD34377719','2023-05-11 09:25:45.646','(court-case-matcher-7)',0),
    (7699, '665597e6-b4d0-466d-98ad-3a9e9eb99b87','3306014916856309133','2023-06-05 12:05:23.135','(court-case-matcher-7)',false,'LIBRA','12GD3124119','2023-06-05 12:05:23.135','(court-case-matcher-7)',0);

INSERT INTO hearing (id, hearing_id,created,created_by,deleted,fk_court_case_id_remove,first_created,hearing_event_type,hearing_type,list_no,fk_court_case_id,last_updated,last_updated_by,"version")
VALUES
    (5002, '57e86555-bd97-43f7-ad1c-55a992b37a2d','2023-05-11 09:25:45.646','(court-case-matcher-7)',false,NULL,'2023-05-11 09:25:45.270','UNKNOWN','Sentence',NULL,6252,'2023-07-18 15:47:08.368','(court-case-matcher-7)',1),
    (5059, '1eb3a6da-8189-4de2-8377-da5910e486b9','2023-06-07 12:06:12.165','(court-case-matcher-7)',false,NULL,now(),'UNKNOWN',NULL,'4th',7699,'2023-06-07 12:06:12.165','(court-case-matcher-7)',0);

INSERT INTO hearing_day (id,fk_hearing_id,hearing_day,hearing_time,court_code,court_room,created,created_by,last_updated,last_updated_by,"version",deleted)
VALUES
    (6570,5059,'2023-07-03','09:01:00','B14LO','01','2023-07-03 10:30:58.028','(court-case-matcher-7)','2023-07-03 10:30:58.028','(court-case-matcher-7)',0,false),
    (6603,5002,'2023-07-03','09:00:00','B14LO','Crown Court 5-1','2023-07-18 16:28:02.824','(court-case-matcher-7)','2023-07-18 16:28:02.824','(court-case-matcher-7)',0,false);

INSERT INTO offender (id, crn,previously_known_termination_date,probation_status,suspended_sentence_order,breach,pre_sentence_activity,awaiting_psr,created,created_by,last_updated,last_updated_by,"version",deleted,pnc,cro)
VALUES (-19, 'X375482',NULL,'CURRENT',false,false,false,false,'2021-12-21 18:51:07.087','(court-case-matcher)','2021-12-21 18:51:07.087','(court-case-matcher)',0,false,NULL,NULL),
       (-10, 'X346204','2022-09-30','CURRENT',false,true,true,true,'2021-11-25 15:37:09.731','(migration)','2023-07-18 15:47:53.166','(court-case-matcher-7)',160,false,NULL,NULL),
       (-21, 'X361020','2023-07-10','PREVIOUSLY_KNOWN',false,false,true,true,'2021-12-22 11:40:02.082','JOHNEVANS(prepare-a-case-for-court)','2023-07-13 16:39:55.822','RAVI(prepare-a-case-for-court-1)',2,false,NULL,NULL);

INSERT INTO defendant (id, defendant_name,"type","name",address,crn,pnc,cro,date_of_birth,sex,nationality_1,nationality_2,created,created_by,manual_update,defendant_id,offender_confirmed,phone_number,person_id,fk_offender_id,last_updated,last_updated_by,"version",deleted,tsv_name)
VALUES
    (5093, 'Mr Arthur Morgan','PERSON','{"title": "Mr", "surname": "Morgan", "forename1": "Arthur"}','{"line1": "13 Wind Street", "line2": "Swansea", "line3": "Wales", "line4": "UK", "line5": "Earth", "postcode": "SA1 1FU"}','X346204',NULL,NULL,'1975-01-01','MALE',NULL,NULL,'2022-10-06 09:35:54.620','(court-case-matcher)',false,'7cece15c-78e8-4be9-a509-35d74eb68839'::uuid,false,'{"home": "+44 114 496 2345", "work": "0114 496 0000", "mobile": "555 CRIME"}','c9abb67c-7113-461f-a6fc-c61616055f2f'::uuid,-10,'2023-07-18 15:47:08.368','(court-case-matcher-7)',5,false,'''arthur'':2 ''morgan'':3 ''mr'':1'::tsvector),
    (5812, 'Mr Jeff Blogs','PERSON','{"title": "Mr", "surname": "Blogs", "forename1": "Jeff"}','{"line1": "39 The Street", "line2": "Newtown", "postcode": "NT4 6YH"}',NULL,'2004/0046583U',NULL,'1975-01-01','NOT_KNOWN',NULL,NULL,'2023-06-05 12:05:23.257','(court-case-matcher-7)',false,'8dc4322f-75de-429b-875b-0063b7c0c044'::uuid,false,NULL,'0169f72a-e713-4d4e-9b85-531708dc928b'::uuid,NULL,'2023-06-05 12:05:23.257','(court-case-matcher-7)',0,false,'''mr'':1 ''ravi'':2 ''testtwo'':3'::tsvector);

INSERT INTO hearing_defendant (id, fk_hearing_id,created,created_by,defendant_id,fk_defendant_id,last_updated,last_updated_by,"version",deleted)
VALUES
    (5920, 5059,'2023-06-07 12:06:12.183','(court-case-matcher-7)','8dc4322f-75de-429b-875b-0063b7c0c044'::uuid,5812,'2023-06-07 12:06:12.183','(court-case-matcher-7)',0,false),
    (5999, 5002,'2023-07-18 15:47:08.359','(court-case-matcher-7)','7cece15c-78e8-4be9-a509-35d74eb68839'::uuid,5093,'2023-07-18 15:47:08.369','(court-case-matcher-7)',1,false);

INSERT INTO offence (id, fk_hearing_defendant_id,summary,title,"sequence",act,created,created_by,list_no,last_updated,last_updated_by,"version",deleted,offence_code,short_term_custody_predictor_score,plea_id,verdict_id)
VALUES (9810, 5920,'On 01/01/2016 at Town, stole Article, to the value of £100.00, belonging to Person.','Offence 102',1,'Contrary to section 1(1) and 7 of the Theft Act 1968.','2023-07-03 10:30:58.031','(court-case-matcher-7)',NULL,'2023-07-03 10:30:58.033','(court-case-matcher-7)',1,false,'MC80528',NULL,NULL,NULL);

INSERT INTO offender_match_group (id, created,last_updated,created_by,last_updated_by,deleted,"version",case_id,defendant_id)
VALUES
    (1891, '2022-03-24 14:50:55.333','2022-03-24 14:50:55.333','(court-case-matcher)','(court-case-matcher)',false,0,'e31a09d3-6284-47b2-bfd7-7d38af17182c','7cece15c-78e8-4be9-a509-35d74eb68839'),
    (3370, '2023-06-05 12:05:24.259','2023-06-05 12:05:24.259','(court-case-matcher-7)','(court-case-matcher-7)',false,0,'665597e6-b4d0-466d-98ad-3a9e9eb99b87','8dc4322f-75de-429b-875b-0063b7c0c044');

INSERT INTO hearing_outcome (id, outcome_type,created,created_by,deleted,last_updated,last_updated_by,"version",outcome_date,state, fk_hearing_id, FK_HEARING_DEFENDANT_ID)
VALUES
    (5, 'REPORT_REQUESTED','2023-07-11 16:55:18.756','AUTH_RO_USER(prepare-a-case-for-court)',false,'2023-07-11 16:55:18.756','AUTH_RO_USER(prepare-a-case-for-court)',0,'2023-07-11 16:55:18.496','NEW', 5087, 5920),
    (4, 'REPORT_REQUESTED','2023-07-11 16:55:18.756','AUTH_RO_USER(prepare-a-case-for-court)',false,'2023-07-11 16:55:18.756','AUTH_RO_USER(prepare-a-case-for-court)',0,'2023-07-11 16:55:18.496','NEW', 5096, 5999);
