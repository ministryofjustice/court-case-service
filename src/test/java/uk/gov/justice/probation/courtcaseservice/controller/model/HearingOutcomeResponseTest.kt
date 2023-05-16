package uk.gov.justice.probation.courtcaseservice.controller.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDateTime

internal class HearingOutcomeResponseTest {
    @Test
    fun shouldMapToHearingOutcomeResponseModel() {
        val outcomeDate = LocalDateTime.of(2022, 12, 12, 12, 12, 12)
        val hearingOutcomeEntity = HearingOutcomeEntity.builder().outcomeType("REPORT_REQUESTED").outcomeDate(
            outcomeDate
        ).build()
        val result = HearingOutcomeResponse.of(hearingOutcomeEntity)
        Assertions.assertThat(result?.hearingOutcomeType)
            .isEqualTo(HearingOutcomeType.REPORT_REQUESTED)
        Assertions.assertThat(result?.outcomeDate).isEqualTo(outcomeDate)
        Assertions.assertThat(result?.getHearingOutcomeDescription()).isEqualTo("Report requested")
    }
    @Test
    fun shouldMapToToNullWhenHearingOutcomeEntityIsNull() {
        Assertions.assertThat(HearingOutcomeResponse.of(null)).isNull()
    }
}