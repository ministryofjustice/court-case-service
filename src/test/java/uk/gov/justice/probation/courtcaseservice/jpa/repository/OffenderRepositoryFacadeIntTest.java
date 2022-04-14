package uk.gov.justice.probation.courtcaseservice.jpa.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.util.AssertionErrors;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.PREVIOUSLY_KNOWN;

@Sql(scripts = {
    "classpath:before-test.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class OffenderRepositoryFacadeIntTest extends BaseRepositoryIntTest {

    @Autowired
    private OffenderRepository offenderRepository;

    private OffenderRepositoryFacade offenderRepositoryFacade;

    @BeforeEach
    void setup() {
        offenderRepositoryFacade = new OffenderRepositoryFacade(offenderRepository);
    }

    @Test
    void shouldUpdateOffenderWhenExists() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        final var CRN = "Z320755";

        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .breach(false)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(testDate)
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        offenderRepositoryFacade.updateOffenderIfItExists(updatedOffender);

        final var actual = offenderRepository.findByCrn(CRN).get();
        assertThat(actual.getCrn()).isEqualTo(CRN);
        assertThat(actual.isBreach()).isEqualTo(false);
        assertThat(actual.getAwaitingPsr()).isEqualTo(false);
        assertThat(actual.getProbationStatus()).isEqualTo(PREVIOUSLY_KNOWN);
        assertThat(actual.getPreviouslyKnownTerminationDate()).isEqualTo(testDate);
        assertThat(actual.isSuspendedSentenceOrder()).isEqualTo(false);
        assertThat(actual.isPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    void shouldReturnInputUpdatedOffenderWhenOffenderDoesNotExists() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        final var CRN = "XXXXX";

        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .breach(false)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(testDate)
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        var actual = offenderRepositoryFacade.updateOffenderIfItExists(updatedOffender);

        final var dbOffender = offenderRepository.findByCrn(CRN);
        assertThat(dbOffender.isEmpty()).isTrue();
        assertThat(actual).isEqualTo(updatedOffender);
    }
}