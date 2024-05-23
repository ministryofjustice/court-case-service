package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest.HearingDefendantsService
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class HearingDefendantsServiceTest {

    companion object {
        const val crn = "B25829"
    }

    @Mock
    lateinit var hearingDefendantRepository: HearingDefendantRepository

    @Mock
    lateinit var hearingOutcomeRepository: HearingOutcomeRepository

    @Mock
    lateinit var hearingNoteRepository: HearingNoteRepository

    private lateinit var hearingDefendantsService: HearingDefendantsService

    @BeforeEach
    fun initTest() {
        hearingDefendantsService = HearingDefendantsService(hearingDefendantRepository, hearingOutcomeRepository, hearingNoteRepository)
    }

    @Test
    fun `given defendant crn it should return hearing outcomes`() {
        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, null, null))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingDefendantsService.getHearingOutcomes(crn, null, null))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "IN_PROGRESS",
                "John Doe",
                LocalDateTime.parse("2024-01-01T00:00")
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, null, null)
    }

    @Test
    fun `given defendant crn and valid date ranges it should return hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingOutcomeCreatedDate.toLocalDate()
        val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingDefendantsService.getHearingOutcomes(crn, hearingOutcomeCreatedDate.toLocalDate(), toDate))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "IN_PROGRESS",
                "John Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given defendant crn and valid fromDate it should return filtered hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingOutcomeCreatedDate.toLocalDate()

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), null))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingDefendantsService.getHearingOutcomes(crn, hearingOutcomeCreatedDate.toLocalDate(), null))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "IN_PROGRESS",
                "John Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), null)
    }

    @Test
    fun `given defendant crn and valid toDate it should return filtered hearing outcomes`() {
        val hearingOutcomeCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val toDate = hearingOutcomeCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingOutcomeRepository.findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, null, toDate.atTime(LocalTime.MAX)))
            .willReturn(listOf(dbHearingDefendantEntity.hearingOutcome))

        Assertions.assertThat(hearingDefendantsService.getHearingOutcomes(crn, null, toDate))
            .isEqualTo(listOf(HearingOutcomeSarResponse(
                "",
                LocalDateTime.parse("2020-05-01T00:00"),
                LocalDateTime.parse("2020-05-01T00:00"),
                "IN_PROGRESS",
                "John Doe",
                hearingOutcomeCreatedDate
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingOutcomeRepository).findAllByHearingDefendantIdAndOutcomeDateBetween(dbHearingDefendantEntity.id, null, toDate.atTime(LocalTime.MAX))
    }

    //

    @Test
    fun `given defendant crn it should return hearing notes`() {
        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, null, null))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingDefendantsService.getHearingNotes(crn, null, null))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Note Taker"
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, null, null)
    }

    @Test
    fun `given defendant crn and valid date ranges it should return hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingDefendantsService.getHearingNotes(crn, hearingNoteCreatedDate.toLocalDate(), toDate))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Note Taker"
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given defendant crn and valid fromDate it should return filtered hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), null))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingDefendantsService.getHearingNotes(crn, hearingNoteCreatedDate.toLocalDate(), null))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Note Taker"
            )))

        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), null)
    }

    @Test
    fun `given defendant crn and valid toDate it should return filtered hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntity()
        BDDMockito.given(hearingDefendantRepository.findAllByDefendantCrn(crn)).willReturn(listOf(dbHearingDefendantEntity))

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, null, toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingDefendantsService.getHearingNotes(crn, null, toDate))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Note Taker"
            )))


        Mockito.verify(hearingDefendantRepository).findAllByDefendantCrn(crn)
        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedIsBetween(dbHearingDefendantEntity.id, null, toDate.atTime(LocalTime.MAX))
    }
}