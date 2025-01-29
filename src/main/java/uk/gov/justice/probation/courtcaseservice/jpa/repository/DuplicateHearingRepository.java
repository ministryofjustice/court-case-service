package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DuplicateHearingRepository {

    private final EntityManager entityManager;

    public DuplicateHearingRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<HearingCourtCaseDTO> findOldestDuplicateHearings() {
        String sql = "WITH RankedHearings AS (" +
                "    SELECT h.id, h.hearing_id, cc.case_id, h.created, " +
                "           ROW_NUMBER() OVER (PARTITION BY h.hearing_id, cc.case_id ORDER BY h.created DESC) as rank " +
                "    FROM hearing h " +
                "    JOIN court_case cc ON h.fk_court_case_id = cc.id " +
                "    WHERE h.created > '2024-11-21 12:00:00' " +
                ") " +
                "SELECT id, hearing_id, case_id, created " +
                "FROM RankedHearings " +
                "WHERE rank > 1 " +
                "ORDER BY hearing_id, case_id, created ASC";

        Query query = entityManager.createNativeQuery(sql);

        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result ->
                        new HearingCourtCaseDTO(
                                ((Number) result[0]).longValue(),
                                (String) result[1],
                                (String) result[2],
                                ((java.sql.Timestamp) result[3]).toLocalDateTime()) ).collect(Collectors.toList());
    }



}
