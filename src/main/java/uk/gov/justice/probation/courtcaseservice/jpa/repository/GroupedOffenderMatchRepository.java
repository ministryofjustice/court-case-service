package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

@Repository
public interface GroupedOffenderMatchRepository extends CrudRepository<GroupedOffenderMatchesEntity, Long> {
    GroupedOffenderMatchesEntity findByCourtCase(CourtCaseEntity courtCase);

    @Query(value = "SELECT group FROM GroupedOffenderMatchesEntity group "
        + "WHERE group.courtCase.courtCode = :courtCode AND group.courtCase.caseNo = :caseNo")
    Optional<GroupedOffenderMatchesEntity> findByCourtCodeAndCaseNo(@Param("courtCode") String courtCode, @Param("caseNo") String caseNo);
}
