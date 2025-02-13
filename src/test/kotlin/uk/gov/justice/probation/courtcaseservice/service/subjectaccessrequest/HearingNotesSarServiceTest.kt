package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
internal class HearingNotesSarServiceTest {

    companion object {
        const val crn = "B25829"
    }

    @Mock
    lateinit var hearingNoteRepository: HearingNoteRepository

    private lateinit var hearingNotesService: HearingNotesSarService

    @BeforeEach
    fun initTest() {
        hearingNotesService = HearingNotesSarService(
            hearingNoteRepository
        )
    }

    @Test
    fun `given hearing defendant it should return hearing notes`() {
        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingNoteRepository.findByHearingDefendantId(dbHearingDefendantEntity.id))
            .willReturn(dbHearingDefendantEntity.notes)

        val authorSurname = "Taker"

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, null, null))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                authorSurname
            )))

        Mockito.verify(hearingNoteRepository).findByHearingDefendantId(dbHearingDefendantEntity.id)
    }

    @Test
    fun `given hearing defendant and valid date ranges it should return hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, hearingNoteCreatedDate.toLocalDate(), toDate))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Taker"
            )))

        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given hearing defendant and valid fromDate it should return filtered hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedAfter(dbHearingDefendantEntity.id, fromDate.atStartOfDay()))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, hearingNoteCreatedDate.toLocalDate(), null))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Taker"
            )))

        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedAfter(dbHearingDefendantEntity.id, fromDate.atStartOfDay())
    }

    @Test
    fun `given hearing defendant and valid toDate it should return filtered hearing notes`() {
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBefore(dbHearingDefendantEntity.id, toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, null, toDate))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Taker"
            )))

        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedBefore(dbHearingDefendantEntity.id, toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given hearing defendant and valid toDate it should return non-draft hearing notes`(){
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, fromDate, toDate))
            .isEqualTo(listOf(HearingNotesSarResponse(
                "UUID",
                "This is a fake note",
                "Taker"
            )))

        Mockito.verify(hearingNoteRepository).findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `given hearing defendant and valid date ranges, it should not return deleted hearing notes`(){
        val hearingNoteCreatedDate = LocalDateTime.parse("2024-01-01T00:00")
        val fromDate = hearingNoteCreatedDate.toLocalDate()
        val toDate = hearingNoteCreatedDate.toLocalDate().plusDays(1)

        val dbHearingDefendantEntity = EntityHelper.aHearingDefendantEntityWithCrn(1, crn)
        dbHearingDefendantEntity.notes.map { it.isDeleted = true }

        BDDMockito.given(hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBetween(dbHearingDefendantEntity.id, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)))
            .willReturn(dbHearingDefendantEntity.notes)

        Assertions.assertThat(hearingNotesService.getHearingNotes(dbHearingDefendantEntity, fromDate, toDate)).isEmpty()
    }

}