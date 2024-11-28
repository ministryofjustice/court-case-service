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
        String sql = "WITH OldestHearings AS (" +
                "    SELECT h.hearing_id, cc.case_id, MIN(h.created) as oldest_created " +
                "    FROM hearing h " +
                "    JOIN court_case cc ON h.fk_court_case_id = cc.id " +
                "    WHERE h.created > '2024-11-21 12:00:00' " +
                "    GROUP BY h.hearing_id, cc.case_id " +
                "    HAVING COUNT(*) > 1 " +
                "), " +
                "DuplicateHearings AS (" +
                "    SELECT h.id, h.hearing_id, h.created, cc.case_id " +
                "    FROM hearing h " +
                "    JOIN court_case cc ON h.fk_court_case_id = cc.id " +
                "    JOIN OldestHearings oh ON h.hearing_id = oh.hearing_id AND h.created = oh.oldest_created AND cc.case_id = oh.case_id " +
                ") " +
                "SELECT dh.id, dh.hearing_id, dh.case_id, dh.created " +
                "FROM DuplicateHearings dh " +
                "ORDER BY dh.created ASC";

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
