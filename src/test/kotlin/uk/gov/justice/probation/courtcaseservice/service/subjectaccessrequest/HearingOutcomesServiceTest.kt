package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import java.time.LocalDateTime
internal class HearingOutcomesServiceTest {

  companion object {
    const val CRN = "B25829"
  }
  private lateinit var hearingOutcomesService: HearingOutcomesService

  @BeforeEach
  fun initTest() {
    hearingOutcomesService = HearingOutcomesService()
  }

  @Test
  fun `given hearing defendant it should return hearing outcomes`() {
    val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, CRN)

    Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, null, null))
      .isEqualTo(
        listOf(
          HearingOutcomeSarResponse(
            "Adjourned",
            LocalDateTime.parse("2020-05-01T00:00"),
            LocalDateTime.parse("2020-05-01T00:00"),
            "In progress",
            "Doe",
            LocalDateTime.parse("2024-01-01T00:00"),
          ),
        ),
      )
  }

  @Test
  fun `given hearing defendant and valid date ranges it should return hearing outcomes`() {
    val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
    val fromDate = hearingOutcomeCreatedDate.toLocalDate()
    val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

    var dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, CRN)

    Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, hearingOutcomeCreatedDate.toLocalDate(), toDate))
      .isEqualTo(
        listOf(
          HearingOutcomeSarResponse(
            "Adjourned",
            LocalDateTime.parse("2020-05-01T00:00"),
            LocalDateTime.parse("2020-05-01T00:00"),
            "In progress",
            "Doe",
            hearingOutcomeCreatedDate,
          ),
        ),
      )
  }

  @Test
  fun `given hearing defendant and valid fromDate it should return filtered hearing outcomes`() {
    val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
    val fromDate = hearingOutcomeCreatedDate.toLocalDate()

    val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, CRN)

    Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, hearingOutcomeCreatedDate.toLocalDate(), null))
      .isEqualTo(
        listOf(
          HearingOutcomeSarResponse(
            "Adjourned",
            LocalDateTime.parse("2020-05-01T00:00"),
            LocalDateTime.parse("2020-05-01T00:00"),
            "In progress",
            "Doe",
            hearingOutcomeCreatedDate,
          ),
        ),
      )
  }

  @Test
  fun `given hearing defendant and valid toDate it should return filtered hearing outcomes`() {
    val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
    val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

    val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, CRN)

    Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, null, toDate))
      .isEqualTo(
        listOf(
          HearingOutcomeSarResponse(
            "Adjourned",
            LocalDateTime.parse("2020-05-01T00:00"),
            LocalDateTime.parse("2020-05-01T00:00"),
            "In progress",
            "Doe",
            hearingOutcomeCreatedDate,
          ),
        ),
      )
  }
}
