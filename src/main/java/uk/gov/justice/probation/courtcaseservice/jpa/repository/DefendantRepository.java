package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DefendantRepository extends CrudRepository<DefendantEntity, Long> {

    Optional<DefendantEntity> findFirstByDefendantId(String defendantId);

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
            "select d.* from defendant d " +
                    "where d.pnc= :pnc and d.dob = :dateOfBirth " +
                    "and name-->> 'firstname' = :foreName and name-->>'surname' = :surName",
            nativeQuery = true)
    List<DefendantEntity> findMatchingDefendants(String foreName, String surName, String pnc, LocalDate dateOfBirth);
}
