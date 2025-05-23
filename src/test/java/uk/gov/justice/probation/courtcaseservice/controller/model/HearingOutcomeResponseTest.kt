package uk.gov.justice.probation.courtcaseservice.controller.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDateTime

internal class HearingOutcomeResponseTest {
  @Test
  fun shouldMapToHearingOutcomeResponseModel() {
    val outcomeDate = LocalDateTime.of(2022, 12, 12, 12, 12, 12)
    val hearingOutcomeEntity = HearingOutcomeEntity.builder().state("RESULTED").outcomeType("REPORT_REQUESTED").outcomeDate(
      outcomeDate,
    ).build()
    val result = HearingOutcomeResponse.of(hearingOutcomeEntity)
    Assertions.assertThat(result?.hearingOutcomeType)
      .isEqualTo(HearingOutcomeType.REPORT_REQUESTED)
    Assertions.assertThat(result?.outcomeDate).isEqualTo(outcomeDate)
    Assertions.assertThat(result?.state).isEqualTo(HearingOutcomeItemState.RESULTED)
    Assertions.assertThat(result?.getHearingOutcomeDescription()).isEqualTo("Report requested")
  }

  @Test
  fun shouldMapToToNullWhenHearingOutcomeEntityIsNull() {
    Assertions.assertThat(HearingOutcomeResponse.of(null)).isNull()
  }

  @Test
  fun `given hearing entity should map to hearing outcome responses`() {
    val hearingOutcomeEntity1 =
      HearingOutcomeEntity.builder().outcomeType(HearingOutcomeType.REPORT_REQUESTED.name).outcomeDate(
        LocalDateTime.of(2023, 6, 6, 19, 9, 1),
      ).resultedDate(
        LocalDateTime.of(2023, 6, 26, 10, 10, 10),
      ).state("IN_PROGRESS").build()

    val hearingDefendantEntity =
      EntityHelper.aHearingDefendantEntity("defendant-id-2", null).withHearingOutcome(hearingOutcomeEntity1)
    val hearing = EntityHelper.aHearingEntity(
      "CRN123",
      "case-no-1",
      listOf(
        EntityHelper.aHearingDefendantEntity("defendant-id-1"),
        hearingDefendantEntity,
      ),
    )

    EntityHelper.refreshMappings(hearing)

    val response = HearingOutcomeResponse.of(hearingDefendantEntity, EntityHelper.SESSION_START_TIME.toLocalDate())

    Assertions.assertThat(response).isEqualTo(
      HearingOutcomeResponse(
        hearingOutcomeType = HearingOutcomeType.REPORT_REQUESTED,
        outcomeDate = LocalDateTime.of(2023, 6, 6, 19, 9, 1),
        resultedDate = LocalDateTime.of(2023, 6, 26, 10, 10, 10),
        hearingDate = EntityHelper.SESSION_START_TIME.toLocalDate(),
        hearingId = EntityHelper.HEARING_ID,
        defendantId = "defendant-id-2",
        probationStatus = "No record",
        offences = listOf(EntityHelper.OFFENCE_TITLE),
        defendantName = EntityHelper.DEFENDANT_NAME,
        crn = null,
        state = HearingOutcomeItemState.IN_PROGRESS,
      ),
    )
  }
}
