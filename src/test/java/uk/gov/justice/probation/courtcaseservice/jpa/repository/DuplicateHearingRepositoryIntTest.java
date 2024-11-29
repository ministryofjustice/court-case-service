package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = {
        "classpath:sql/before-common.sql",
        "classpath:sql/before-HearingRepositoryDeleteIntTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class DuplicateHearingRepositoryIntTest extends BaseIntTest {
    private DuplicateHearingRepository duplicateHearingRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        duplicateHearingRepository = new DuplicateHearingRepository(entityManager);
    }

    @Test
    void findDuplicateRecords() {
        List<HearingCourtCaseDTO> hearingEntities = duplicateHearingRepository.findOldestDuplicateHearings();
        assertThat(hearingEntities).hasSize(1);
        assertThat(hearingEntities.get(0).getId()).isEqualTo(-198);
        assertThat(hearingEntities.get(0).getCreated()).isEqualTo(
                LocalDateTime.of(2024, 11, 23, 17, 59, 59, 0));
    }
}
