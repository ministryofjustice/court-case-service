package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

import java.util.Optional;

@Repository
public interface GroupedOffenderMatchRepository extends CrudRepository<GroupedOffenderMatchesEntity, Long> {
    Optional<GroupedOffenderMatchesEntity> findByCaseIdAndDefendantId(String caseId, String defendantId);

    Optional<GroupedOffenderMatchesEntity> findFirstByDefendantIdOrderByIdDesc(String defendantId);

    @Query(value = "select count(om.id) from offender_match om "
        + "INNER join offender_match_group omg "
        + "on omg.id = om.group_id "
        + "where omg.case_id = :caseId "
        + "and omg.defendant_id = :defendantId",
        nativeQuery = true)
    Optional<Integer> getMatchCountByCaseIdAndDefendant(String caseId, String defendantId);
}
