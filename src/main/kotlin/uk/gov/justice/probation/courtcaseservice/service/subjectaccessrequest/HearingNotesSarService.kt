package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class HearingNotesSarService(
    val hearingNoteRepository: HearingNoteRepository
) {

    fun getHearingNotes(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?,toDate: LocalDate?): List<HearingNotesSarResponse> {
        val hearingNotes = getFilteredHearingNotes(hearingDefendant, fromDate, toDate)

        return hearingNotesResponse(hearingNotes)
    }

    private fun hearingNotesResponse(hearingNotes: List<HearingNoteEntity>): List<HearingNotesSarResponse> {
        return hearingNotes.filter {
            note -> !note.isDraft && !note.isDeleted
        }.map { note -> HearingNotesSarResponse(
            note.note,
            getSurname(note.author)
        ) }
    }

    private fun getSurname(name: String): String {
        return name.split(" ").last()
    }

    private fun getFilteredHearingNotes(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?, toDate: LocalDate?): List<HearingNoteEntity> {
        return filterHearingNotesByDate(hearingDefendant, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))
    }

    private fun filterHearingNotesByDate(hearingDefendant: HearingDefendantEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingNoteEntity> {
        if(fromDate != null && toDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBetween(
                hearingDefendant.id,
                fromDate,
                toDate
            )
        } else if(fromDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedAfter(
                hearingDefendant.id,
                fromDate
            )
        } else if(toDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBefore(
                hearingDefendant.id,
                toDate
            )
        }
        return hearingNoteRepository.findByHearingDefendantId(hearingDefendant.id)
    }
}