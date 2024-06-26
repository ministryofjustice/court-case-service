package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DefendantRepository extends CrudRepository<DefendantEntity, Long> {

    Optional<DefendantEntity> findFirstByDefendantId(String defendantId);

    List<DefendantEntity> findAllByCrn(String crn);

    @Query(value = "select d.* from defendant d, offender o " +
            "where o.crn = :crn " +
            "and d.fk_offender_id = o.id ",
            nativeQuery = true)
    Page<DefendantEntity> findDefendantsByCrn(String crn, Pageable pageable);

    @Query(value =
            "select d.* from defendant d " +
                    "where d.tsv_name @@ to_tsquery(:tsQueryString) " +
                    "order by similarity (d.defendant_name, :name) desc",
            countQuery =
                    "select count(*) from defendant d " +
                            "where d.tsv_name @@ to_tsquery(:tsQueryString) ",
            nativeQuery = true)
    Page<DefendantEntity> findDefendantsByName(String tsQueryString, String name, Pageable pageable);

    @Query(value =
                "select * from defendant " +
                        "where pnc= :pnc and date_of_birth = :dateOfBirth " +
                        "and lower(name->>'forename1') = lower(:forename) and lower(name->>'surname') = lower(:surname)",
                nativeQuery = true)
    List<DefendantEntity> findMatchingDefendants(String pnc, LocalDate dateOfBirth, String forename, String surname);
}
