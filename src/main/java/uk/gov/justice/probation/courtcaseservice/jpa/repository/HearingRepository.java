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
public interface HearingRepository extends CrudRepository<HearingEntity, Long>, HearingSearchRepositoryCustom {

    Optional<HearingEntity> findFirstByHearingId(String hearingId);

    Optional<HearingEntity> findFirstByHearingDefendantsDefendantId(String defendantId);

    @Query(value = "select h.* from court_case cc " +
        "join hearing h on cc.id = h.fk_court_case_id " +
        "join hearing_day hd on h.id = hd.fk_hearing_id " +
        "inner join (select max(hearing.id) as max_id from hearing " +
        "   join court_case on court_case.id = hearing.fk_court_case_id " +
        "   join hearing_day on hearing.id = hearing_day.fk_hearing_id " +
        "   where court_case.case_no = :caseNo and court_case.deleted = false " +
        "   and coalesce(hearing.list_no, '') = coalesce(:listNo, '') " +
        "   and hearing_day.court_code = :courtCode " +
        "   group by court_case.case_no, hearing_day.court_code) grouped_cases " +
        "on h.id = grouped_cases.max_id ",
            nativeQuery = true)
    Optional<HearingEntity> findByCourtCodeCaseNoAndListNo(String courtCode, String caseNo, String listNo);

    @Query(value = "select h.* from court_case cc " +
        "join hearing h on cc.id = h.fk_court_case_id " +
        "join hearing_day hd on h.id = hd.fk_hearing_id " +
        "inner join (select max(hearing.id) as max_id from hearing " +
        "   join court_case on court_case.id = hearing.fk_court_case_id " +
        "   join hearing_day on hearing.id = hearing_day.fk_hearing_id " +
        "   where court_case.case_no = :caseNo and court_case.deleted = false " +
        "   and hearing_day.court_code = :courtCode " +
        "   group by court_case.case_no, hearing_day.court_code) grouped_cases " +
        "on h.id = grouped_cases.max_id ",
        nativeQuery = true)
    Optional<HearingEntity> findMostRecentByCourtCodeAndCaseNo(String courtCode, String caseNo);

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

    @Query(value = "select h.* from hearing h " +
        "inner join hearing_day hday on hday.fk_hearing_id = h.id " +
        "inner join hearing_outcome ho on h.fk_hearing_outcome = ho.id " +
        "where hday.court_code = :courtCode and ho.state = :hearingOutcomeItemState",
        nativeQuery = true)
    List<HearingEntity> findByCourtCodeAndHearingOutcome(
        String courtCode,
        String hearingOutcomeItemState
    );

    @Query(value = "select count(h.id) from " +
        "hearing_day hday " +
        "join hearing h on h.id = hday.fk_hearing_id " +
        "and hday.hearing_day = :date " +
        "and hday.court_code = :courtCode " +
        "and date(h.first_created) = date(now())",
        nativeQuery = true)
    Optional<Integer> getRecentlyAddedCasesCount(String courtCode, LocalDate date);

    @Query(value = "select distinct court_room from hearing_day " +
        "where hearing_day = :date and court_code = :courtCode " +
        "order by court_room",
        nativeQuery = true)
    List<String> getCourtroomsForCourtAndHearingDay(String courtCode, LocalDate date);
}
