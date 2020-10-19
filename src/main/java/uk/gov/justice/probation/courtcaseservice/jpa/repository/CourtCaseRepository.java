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
//    TODO: Update this to return first_created
    Optional<CourtCaseEntity> findTopByCourtCodeAndCaseNoOrderByCreatedDesc(String courtCode, String caseNo);

//    TODO: Return new first_created value in CourtCaseEntity
//    @Query(value="select cc.*, grouped_cases.min_created as first_created from court_case cc " +
//            "inner join (select min(created) as min_created, max(created) as max_created, case_no from court_case group_cc " +
//            "where session_start_time >= :start " +
//            "and session_start_time < :end " +
//            "and created >= :createdAfter " +
//            "and group_cc.court_code = :courtCode " +
//            "group by case_no, court_code) grouped_cases " +
//            "on cc.case_no = grouped_cases.case_no " +
//            "and cc.created = grouped_cases.max_created",
//            nativeQuery=true)
    @Query(value="select cc.* from court_case cc " +
            "inner join (select max(created) as max_created, case_no from court_case group_cc " +
                "where session_start_time >= :start " +
                "and session_start_time < :end " +
                "and created >= :createdAfter " +
                "and group_cc.court_code = :courtCode " +
                "group by case_no, court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and cc.created = grouped_cases.max_created",
            nativeQuery=true)
    List<CourtCaseEntity> findByCourtCodeAndSessionStartTimeBetweenAndCreatedAfter(String courtCode, LocalDateTime start, LocalDateTime end, LocalDateTime createdAfter);

    @Query(value = "Select cc FROM CourtCaseEntity cc "
        + "WHERE cc.courtCode = :courtCode "
        + "AND cc.sessionStartTime >= :start AND cc.sessionStartTime <= :end "
        + "AND cc.caseNo NOT IN :caseNos")
    List<CourtCaseEntity> findCourtCasesNotIn(@Param("courtCode") String courtCode,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("caseNos") Collection<String> caseNos);
}
