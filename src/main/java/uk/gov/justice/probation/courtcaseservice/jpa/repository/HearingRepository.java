package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    @Query(value = "SELECT hearing.*, null as first_created FROM hearing " +
        "join court_case " +
        "on court_case.id = hearing.fk_court_case_id " +
        "where court_case.case_id  = :caseId " +
        "and hearing.deleted = false " +
        "order by hearing.id desc LIMIT 1",
        nativeQuery = true)
    Optional<HearingEntity> findFirstByCaseIdOrderByIdDesc(String caseId);

    @Query(value = "select h.*, grouped_cases.min_created as first_created from court_case cc " +
            "join hearing h on cc.id = h.fk_court_case_id " +
            "join hearing_day hd on h.id = hd.fk_hearing_id " +
            "inner join (select max(court_case.created) as max_created,  min(court_case.created) as min_created, court_case.case_no, hearing_day.court_code from court_case " +
            "   join hearing on court_case.id = hearing.fk_court_case_id " +
            "   join hearing_day on hearing.id = hearing_day.fk_hearing_id " +
            "   where court_case.case_no = :caseNo " +
            "   and hearing_day.court_code = :courtCode " +
            "   group by court_case.case_no, hearing_day.court_code) grouped_cases " +
            "on cc.created = grouped_cases.max_created " +
            "and cc.case_no = grouped_cases.case_no " +
            "where cc.deleted = false " +
            "order by cc.id desc limit 1",
            nativeQuery = true)
    Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    @Query(value = "select h.*, grouped_cases.min_created as first_created from court_case cc " +
                    "join hearing h on cc.id = h.fk_court_case_id " +
                    "inner join " +
                    "   (select max(court_case.created) as max_created,  min(court_case.created) as min_created, court_case.case_id as case_id from court_case " +
                    "   join hearing on court_case.id = hearing.fk_court_case_id " +
                    "   where court_case.case_id = :caseId " +
                    "   group by court_case.case_id) grouped_cases " +
                    "on cc.created = grouped_cases.max_created " +
                    "and cc.case_id = grouped_cases.case_id " +
                    "where h.deleted = false " +
                    "order by h.id desc limit 1",
        nativeQuery = true)
    Optional<HearingEntity> findByCaseId(String caseId);

    // TODO - need to get CourtCaseEntity values for CRN, etc, from defendant
//    @Query(value = "select hearing.*, grouped_hearings.min_created as first_created " +
//        "from hearing" +
//        "   inner join " +
//        "       (select min(group_hearing.created) as min_created, max(group_hearing.created) as max_created, group_hearing.hearing_id from hearing group_hearing " +
//        "           inner join defendant d on d.fk_hearing_id = group_hearing.id  " +
//        "           where d.defendant_id = :defendantId " +
//        "           and group_hearing.hearing_id = :caseId " +
//        "           group by group_hearing.hearing_id) grouped_hearings " +
//        "       on hearing.fk_court_case_id = grouped_hearings.hearing_id " +
//        "and hearing.hearing_id = :caseId " +
//        "and hearing.created = grouped_hearings.max_created " +
//        "and hearing.deleted = false " +
//        "order by hearing.id desc limit 1",
//        nativeQuery = true)
    @Query(value = "select h.*, grouped_cases.min_created as something_else from court_case cc " +
            "join hearing h on cc.id = h.fk_court_case_id " +
            "inner join " +
            "   (select max(court_case.created) as max_created,  min(court_case.created) as min_created, court_case.case_id from court_case " +
            "   join hearing on court_case.id = hearing.fk_court_case_id " +
            "   inner join defendant d on hearing.id = d.fk_hearing_id  " +
            "   where court_case.case_id = :caseId " +
            "   and d.defendant_id = :defendantId " +
            "   group by court_case.case_id) grouped_cases " +
            "on cc.created = grouped_cases.max_created " +
            "and cc.case_id = grouped_cases.case_id " +
            "where h.deleted = false " +
            "order by h.id desc limit 1",
            nativeQuery = true)
    Optional<HearingEntity> findByCaseIdAndDefendantId(String caseId, String defendantId);


    @Query(value = "select h.*, grouped_hearings.min_created as first_created " +
        "from hearing h " +
        "   inner join " +
        "       (select min(group_hearing.created) as min_created, max(group_hearing.created) as max_created, group_hearing.fk_court_case_id from hearing group_hearing " +
        "           inner join hearing_day on hearing_day.fk_hearing_id = group_hearing.id " +
        "           where group_hearing.created >= :createdAfter and group_hearing.created < :createdBefore " +
        "           and hearing_day.court_code = :courtCode " +
        "           group by group_hearing.fk_court_case_id) grouped_hearings " +
        "       on h.fk_court_case_id = grouped_hearings.fk_court_case_id " +
        "   inner join hearing_day hday on hday.fk_hearing_id = h.id " +
        "where hday.hearing_day = :hearingDay " +
        "and h.created = grouped_hearings.max_created " +
        "and h.deleted = false",
        nativeQuery = true)
    List<HearingEntity> findByCourtCodeAndHearingDay(
        String courtCode,
        LocalDate hearingDay,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore
    );

    @Query(value = "select hearing_day.created from hearing_day " +
        "where hearing_day.court_code = :courtCode " +
        "and hearing_day.hearing_day = :hearingDay " +
        "order by created desc limit 1",
        nativeQuery = true)
    Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay);

}
