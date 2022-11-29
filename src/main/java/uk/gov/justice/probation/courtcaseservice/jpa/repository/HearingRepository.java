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
public interface HearingRepository extends CrudRepository<HearingEntity, Long>{

    Optional<HearingEntity> findFirstByHearingId(String hearingId);

    Optional<HearingEntity> findFirstByHearingDefendantsDefendantId(String defendantId);


    @Query(value = "select h.* from court_case cc " +
            "join hearing h on cc.id = h.fk_court_case_id " +
            "join hearing_day hd on h.id = hd.fk_hearing_id " +
            "inner join (select max(court_case.id) as max_id, court_case.case_no, hearing_day.court_code from court_case " +
            "   join hearing on court_case.id = hearing.fk_court_case_id " +
            "   join hearing_day on hearing.id = hearing_day.fk_hearing_id " +
            "   where court_case.case_no = :caseNo " +
            "   and hearing.list_no = :listNo " +
            "   and hearing_day.court_code = :courtCode " +
            "   group by court_case.case_no, hearing_day.court_code) grouped_cases " +
            "on cc.id = grouped_cases.max_id " +
            "and cc.case_no = grouped_cases.case_no " +
            "where cc.deleted = false " +
            "order by cc.id desc limit 1",
            nativeQuery = true)
    Optional<HearingEntity> findByCourtCodeCaseNoAndListNo(String courtCode, String caseNo, String listNo);

    @Query(value = "select h.* from court_case cc " +
            "join hearing h on cc.id = h.fk_court_case_id " +
            "join hearing_day hd on h.id = hd.fk_hearing_id " +
            "inner join (select max(court_case.id) as max_id, court_case.case_no, hearing_day.court_code from court_case " +
            "   join hearing on court_case.id = hearing.fk_court_case_id " +
            "   join hearing_day on hearing.id = hearing_day.fk_hearing_id " +
            "   where court_case.case_no = :caseNo " +
            "   and hearing_day.court_code = :courtCode " +
            "   group by court_case.case_no, hearing_day.court_code) grouped_cases " +
            "on cc.id = grouped_cases.max_id " +
            "and cc.case_no = grouped_cases.case_no " +
            "where cc.deleted = false " +
            "order by cc.id desc limit 1",
            nativeQuery = true)
    Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    @Query(value = "select h.* as first_created " +
        "from hearing h " +
        "inner join hearing_day hday on hday.fk_hearing_id = h.id " +
        "where h.created >= :createdAfter and h.created < :createdBefore " +
        "and hday.court_code = :courtCode " +
        "and hday.hearing_day = :hearingDay " +
        "and h.deleted = false",
        nativeQuery = true)
    List<HearingEntity> findByCourtCodeAndHearingDay(
        String courtCode,
        LocalDate hearingDay,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore
    );

    @Query(value = "select h.* as first_created  " +
        "from hearing h  " +
        "inner join hearing_day hday on hday.fk_hearing_id = h.id  " +
        "where hday.hearing_day = :hearingDay and hday.court_code = :courtCode " +
        "and h.deleted = false",
        nativeQuery = true)
    List<HearingEntity> findByCourtCodeAndHearingDay(
        String courtCode,
        LocalDate hearingDay
    );

    @Query(value = "select hearing_day.created from hearing_day " +
        "where hearing_day.court_code = :courtCode " +
        "and hearing_day.hearing_day = :hearingDay " +
        "order by created desc limit 1",
        nativeQuery = true)
    Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay);

    @Query(value = "select * from hearing where id in (" +
        "select max(h.id) from hearing h, court_case cc where " +
        "cc.case_id = :caseId " +
        "and h.fk_court_case_id = cc.id group by h.hearing_id)",
        nativeQuery = true)
    Optional<List<HearingEntity>> findHearingsByCaseId(String caseId);
}
