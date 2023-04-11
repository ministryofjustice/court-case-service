package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import java.util.List;
import java.util.Optional;

public interface DefendantRepository extends CrudRepository<DefendantEntity, Long> {
    Optional<DefendantEntity> findFirstByDefendantId(String defendantId);

    @Query(value = "select d.* from defendant d, offender o " +
            "where o.crn = :crn " +
            "and d.fk_offender_id = o.id ",
        nativeQuery = true)
    List<DefendantEntity> findDefendantsByCrn(String crn);

    @Query(value =
        "select d.* from defendant d " +
        "where to_tsvector(d.defendant_name) @@ to_tsquery(:tsQueryString) " +
        "order by similarity (d.defendant_name, :name) desc",
        nativeQuery = true)
    List<DefendantEntity> findDefendantsByName(String tsQueryString, String name);
}
