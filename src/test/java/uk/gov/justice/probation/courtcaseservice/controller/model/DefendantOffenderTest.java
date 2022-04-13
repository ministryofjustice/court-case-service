package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;

class DefendantOffenderTest {

    @Test
    void shouldMapToEntityCorrectly() {
        LocalDate now = LocalDate.now();
        final var actual = DefendantOffender.builder()
                .probationStatus(CURRENT)
                .preSentenceActivity(true)
                .suspendedSentenceOrder(true)
                .breach(true)
                .awaitingPsr(false)
                .previouslyKnownTerminationDate(now)
                .crn("crn-one")
                .build();

        assertThat(actual.asEntity()).isEqualTo(
                OffenderEntity.builder()
                        .probationStatus(CURRENT)
                        .preSentenceActivity(true)
                        .suspendedSentenceOrder(true)
                        .breach(true)
                        .awaitingPsr(false)
                        .previouslyKnownTerminationDate(now)
                        .crn("crn-one")
                        .build());
    }

    @Test
    void shouldMapToDtoCorrectly() {
        LocalDate now = LocalDate.now();
        final var actual = OffenderEntity.builder()
                .probationStatus(CURRENT)
                .preSentenceActivity(true)
                .suspendedSentenceOrder(true)
                .breach(true)
                .awaitingPsr(false)
                .previouslyKnownTerminationDate(now)
                .crn("crn-one")
                .build();

        assertThat(DefendantOffender.of(actual)).isEqualTo(
                DefendantOffender.builder()
                        .probationStatus(CURRENT)
                        .preSentenceActivity(true)
                        .suspendedSentenceOrder(true)
                        .breach(true)
                        .awaitingPsr(false)
                        .previouslyKnownTerminationDate(now)
                        .crn("crn-one")
                        .build());
    }
}