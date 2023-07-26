package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;

import java.time.LocalDate;
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

    @Query(value = "select count(*) from ( " +
        "select count(omg1.defendant_id) as match_count " +
        "    from hearing_day hday1 " +
        "    join hearing_defendant hd1 on hd1.fk_hearing_id = hday1.fk_hearing_id and hday1.hearing_day = :date and hday1.court_code = :courtCode " +
        "    join defendant d1 on hd1.fk_defendant_id = d1.id and d1.offender_confirmed is false and d1.fk_offender_id is null  " +
        "    join offender_match_group omg1 on omg1.defendant_id = text(hd1.defendant_id) " +
        "    join offender_match om1 on om1.group_id = omg1.id " +
        "   group by omg1.defendant_id, omg1.case_id" +
        ") grouped_matches",
        nativeQuery = true)
    Optional<Integer> getPossibleMatchesCountByDate(String courtCode, LocalDate date);
}
