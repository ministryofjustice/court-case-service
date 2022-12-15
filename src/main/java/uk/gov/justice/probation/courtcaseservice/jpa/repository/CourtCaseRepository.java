package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long>{
    Optional<CourtCaseEntity> findFirstByCaseIdOrderByIdDesc(String caseId);
    @Query(value = "select distinct c.* from court_case c, hearing h, hearing_defendant hd, defendant d, offender o " +
        "where o.crn = :crn " +
        "and d.fk_offender_id = o.id  " +
        "and hd.fk_defendant_id = d.id  " +
        "and h.id = hd.fk_hearing_id  " +
        "and c.id = h.fk_court_case_id",
    nativeQuery = true)
    List<CourtCaseEntity> findAllCasesByCrn(String crn);
}
