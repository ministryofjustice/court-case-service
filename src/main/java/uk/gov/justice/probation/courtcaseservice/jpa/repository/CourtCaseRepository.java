package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

@Repository
public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long> {
    Optional<CourtCaseEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    List<CourtCaseEntity> findByCourtCodeAndSessionStartTimeBetween(String courtCode, LocalDateTime start, LocalDateTime end);

    @Query(value = "Select cc FROM CourtCaseEntity cc "
        + "WHERE cc.courtCode = :courtCode "
        + "AND cc.sessionStartTime >= :start AND cc.sessionStartTime <= :end "
        + "AND cc.caseNo NOT IN :caseNos")
    List<CourtCaseEntity> findCourtCasesNotIn(@Param("courtCode") String courtCode,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("caseNos") Collection<String> caseNos);
}
