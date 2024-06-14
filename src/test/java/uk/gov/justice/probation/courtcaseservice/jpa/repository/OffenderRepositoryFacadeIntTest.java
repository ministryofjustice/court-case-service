package uk.gov.justice.probation.courtcaseservice.jpa.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderEntityInitService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.PREVIOUSLY_KNOWN;

@Sql(scripts = {
    "classpath:before-test.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class OffenderRepositoryFacadeIntTest extends BaseIntTest {

    private static final String CRO = "CRO007";
    private static final String PNC = "PNC007";
    @Autowired
    private OffenderRepository offenderRepository;

    @Autowired
    private OffenderEntityInitService offenderEntityInitService;

    private OffenderRepositoryFacade offenderRepositoryFacade;

    @BeforeEach
    void setUp() {
        offenderRepositoryFacade = new OffenderRepositoryFacade(offenderRepository, offenderEntityInitService);
    }

    @Test
    void shouldUpdateAndSaveOffenderWhenExists() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        final var CRN = "Z320755";

        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .cro(CRO)
            .pnc(PNC)
            .breach(false)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(testDate)
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        offenderRepositoryFacade.save(updatedOffender);

        final var actual = offenderRepository.findByCrn(CRN).get();
        assertThat(actual.getCrn()).isEqualTo(CRN);
        assertThat(actual.getCro()).isEqualTo(CRO);
        assertThat(actual.getPnc()).isEqualTo(PNC);
        assertThat(actual.isBreach()).isEqualTo(false);
        assertThat(actual.getAwaitingPsr()).isEqualTo(false);
        assertThat(actual.getProbationStatus()).isEqualTo(PREVIOUSLY_KNOWN);
        assertThat(actual.getPreviouslyKnownTerminationDate()).isEqualTo(testDate);
        assertThat(actual.isSuspendedSentenceOrder()).isEqualTo(false);
        assertThat(actual.isPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    void shouldSaveAndReturnOffenderWhenOffenderDoesNotExist() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        final var CRN = "XXXXX";
        var dbOffender = offenderRepository.findByCrn(CRN);
        assertThat(dbOffender.isEmpty()).isTrue();

        final var updatedOffender = OffenderEntity.builder()
            .crn(CRN)
            .cro(CRO)
            .pnc(PNC)
            .breach(false)
            .awaitingPsr(false)
            .probationStatus(PREVIOUSLY_KNOWN)
            .previouslyKnownTerminationDate(testDate)
            .suspendedSentenceOrder(false)
            .preSentenceActivity(true)
            .build();

        var actual = offenderRepositoryFacade.save(updatedOffender);

        dbOffender = offenderRepository.findByCrn(CRN);
        assertThat(dbOffender.isEmpty()).isFalse();
        assertThat(actual).isEqualTo(updatedOffender);
    }
}