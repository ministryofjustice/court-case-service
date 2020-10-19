package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long> {
    @Query(value="select cc.*, grouped_cases.min_created as first_created from court_case cc " +
            "inner join (select max(created) as max_created,  min(created) as min_created, case_no from court_case group_cc " +
            "where group_cc.case_no = :caseNo " +
            "and group_cc.court_code = :courtCode " +
            "group by case_no, court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and cc.created = grouped_cases.max_created",
            nativeQuery=true)
    Optional<CourtCaseEntity> findTopByCourtCodeAndCaseNoOrderByCreatedDesc(String courtCode, String caseNo);

    @Query(value="select cc.*, grouped_cases.min_created as first_created from court_case cc " +
            "inner join (select min(created) as min_created, max(created) as max_created, case_no from court_case group_cc " +
            "where session_start_time >= :sessionStartAfter " +
            "and session_start_time < :sessionStartBefore " +
            "and created >= :createdAfter " +
            "and group_cc.court_code = :courtCode " +
            "group by case_no, court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and cc.created = grouped_cases.max_created",
            nativeQuery=true)
    List<CourtCaseEntity> findByCourtCodeAndSessionStartTime(String courtCode, LocalDateTime sessionStartAfter, LocalDateTime sessionStartBefore, LocalDateTime createdAfter);

    @Query(value = "Select cc FROM CourtCaseEntity cc "
        + "WHERE cc.courtCode = :courtCode "
        + "AND cc.sessionStartTime >= :start AND cc.sessionStartTime <= :end "
        + "AND cc.caseNo NOT IN :caseNos")
    List<CourtCaseEntity> findCourtCasesNotIn(@Param("courtCode") String courtCode,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("caseNos") Collection<String> caseNos);
}
