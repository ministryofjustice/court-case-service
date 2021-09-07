BEGIN;
    DROP TABLE IF EXISTS COURT_CASE_CLONE;
    DROP TABLE IF EXISTS defendant_offence_archive;
    DROP TABLE IF EXISTS defendant_archive;
    DROP TABLE IF EXISTS offence_archive;
    DROP TABLE IF EXISTS hearing_archive;
    DROP TABLE IF EXISTS court_case_archive;
    DROP TABLE IF EXISTS offender_match_archive;
    DROP TABLE IF EXISTS offender_match_group_archive;

    -- defendant_offence
    create table defendant_offence_archive as
        select * from defendant_offence do1 where defendant_id in
        (select id from defendant d where court_case_id in (select id from court_case cc where cc.created <= '2021-06-01 00:00:00'));

   -- DEFENDANT
    create table defendant_archive as
        select * from defendant d where court_case_id in (select id from court_case cc where cc.created <= '2021-06-01 00:00:00');

   -- OFFENCE
    create table offence_archive as
        select * from offence o where court_case_id in (select id from court_case cc where cc.created <= '2021-06-01 00:00:00');

   -- HEARING
    create table hearing_archive as
        select * from hearing h where court_case_id in (select id from court_case cc where cc.created <= '2021-06-01 00:00:00');

   -- COURT_CASE
    create table court_case_archive as
        select * from court_case cc where cc.created <= '2021-06-01 00:00:00';

    -- DELETIONS. No cascade delete so need to do in the correct order
    delete from defendant_offence do1 where defendant_id in
        (select id from defendant d where court_case_id in (select id from court_case cc where cc.created <= '2021-06-01 00:00:00'));
    delete from defendant d where court_case_id in
        (select id from court_case cc where cc.created <=  '2021-06-01 00:00:00');
    delete from offence o where court_case_id in
        (select id from court_case cc where cc.created <=  '2021-06-01 00:00:00');
    delete from hearing h where court_case_id in
        (select id from court_case cc where cc.created <=  '2021-06-01 00:00:00');
    delete from court_case cc where cc.created <= '2021-06-01 00:00:00';

    -- OFFENDER_MATCH and GROUP.
    -- NO FK relationship between offender_match_group (omg) and court_case (cc) so look for case no / court codes not in CC which are in OMG
    create table offender_match_archive as
        select * from offender_match om where group_id not in (
            select omg2.id from offender_match_group omg2 , court_case cc where cc.case_no = omg2.case_no and cc.court_code = omg2.court_code
        );

    create table offender_match_group_archive as
        select * from offender_match_group omg where id not in (
                select omg2.id from offender_match_group omg2 , court_case cc where cc.case_no = omg2.case_no and cc.court_code = omg2.court_code
        );

    delete from offender_match om where group_id not in (
        select omg2.id from offender_match_group omg2 , court_case cc where cc.case_no = omg2.case_no and cc.court_code = omg2.court_code
    );

    delete from offender_match_group omg where id not in (
        select omg2.id from offender_match_group omg2 , court_case cc where cc.case_no = omg2.case_no and cc.court_code = omg2.court_code
    );

COMMIT;
