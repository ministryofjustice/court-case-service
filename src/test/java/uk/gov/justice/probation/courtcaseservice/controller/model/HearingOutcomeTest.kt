package uk.gov.justice.probation.courtcaseservice.controller.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

internal class HearingOutcomeTest {
    @Test
    fun shouldMapToHearingOutcomeModel() {
        val hearingOutcomeEntity = HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").build()
        assertThat(HearingOutcome.of(hearingOutcomeEntity))
            .isEqualTo(HearingOutcome(HearingOutcomeType.REPORT_REQUESTED))
    }
    @Test
    fun shouldMapToToNullWhenHearingOutcomeEntityIsNull() {
        assertThat(HearingOutcome.of(null)).isNull()
    }
}