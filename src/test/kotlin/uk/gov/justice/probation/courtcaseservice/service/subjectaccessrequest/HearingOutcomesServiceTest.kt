package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
internal class HearingOutcomesServiceTest {

    companion object {
        const val crn = "B25829"
    }

    @Mock
    lateinit var hearingOutcomeRepository: HearingOutcomeRepository


    private lateinit var hearingOutcomesService: HearingOutcomesService

    @BeforeEach
    fun initTest() {
        hearingOutcomesService = HearingOutcomesService(
            hearingOutcomeRepository
        )
    }

    @Test
    fun `given hearing defendant it should return hearing outcomes`() {
        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingOutcomeRepository.findByHearingDefendantId(dbHearingDefendantEntity.id))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, null, null))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "Adjourned",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "In progress",
                "Doe",
                LocalDateTime.parse("2024-01-01T00:00")
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findByHearingDefendantId(dbHearingDefendantEntity.id)
    }

    @Test
    fun `given hearing defendant and valid date ranges it should return hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingOutcomeCreatedDate.toLocalDate()
        val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

        var dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, hearingOutcomeCreatedDate.toLocalDate(), toDate))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "Adjourned",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "In progress",
                "Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given hearing defendant and valid fromDate it should return filtered hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingOutcomeCreatedDate.toLocalDate()

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedAfter(dbHearingDefendantEntity.id, fromDate.atStartOfDay()))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, hearingOutcomeCreatedDate.toLocalDate(), null))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "Adjourned",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "In progress",
                "Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndCreatedAfter(dbHearingDefendantEntity.id, fromDate.atStartOfDay())
    }

    @Test
    fun `given hearing defendant and valid toDate it should return filtered hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBefore(dbHearingDefendantEntity.id, toDate.atTime(LocalTime.MAX)))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingOutcomesService.getHearingOutcomes(dbHearingDefendantEntity, null, toDate))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "Adjourned",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "In progress",
                "Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndCreatedBefore(dbHearingDefendantEntity.id, toDate.atTime(LocalTime.MAX))
    }
}