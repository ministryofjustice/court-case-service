package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

import java.util.Optional;

@Repository
public interface GroupedOffenderMatchRepository extends CrudRepository<GroupedOffenderMatchesEntity, Long> {
    Optional<GroupedOffenderMatchesEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    @Query(value = "select count(om.id) from offender_match om "
        + "INNER join offender_match_group omg "
        + "on omg.id = om.group_id "
        + "where omg.case_no = :caseNo "
        + "and omg.court_code = :courtCode",
        nativeQuery = true)
    Optional<Integer> getMatchCount(String courtCode, String caseNo);
}
