package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

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
    Optional<CourtCaseEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

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

    @Query(value="select cc.*, grouped_cases.min_created as first_created from court_case cc "
        + "inner join (select min(created) as min_created, max(created) as max_created, case_no from court_case group_cc "
        + "where crn = :crn "
        + "and case_no != :caseNo "
        + "and session_start_time >= current_date "
        + "group by case_no, court_code) grouped_cases "
        + "on cc.case_no = grouped_cases.case_no "
        + "and cc.created = grouped_cases.max_created",
        nativeQuery=true)
    List<CourtCaseEntity> findOtherCurrentCasesByCrn(String crn, String caseNo);
}
