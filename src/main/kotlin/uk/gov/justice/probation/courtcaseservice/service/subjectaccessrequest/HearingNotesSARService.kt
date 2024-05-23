package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class HearingNotesSARService(
    val hearingDefendantRepository: HearingDefendantRepository,
    val hearingNoteRepository: HearingNoteRepository
) {

    fun getHearingNotes(crn: String,fromDate: LocalDate?,toDate: LocalDate?): List<HearingNotesSarResponse> {
        val hearingNotes = getFilteredHearingNotes(getHearingDefendants(crn), fromDate, toDate)

        return hearingNotesResponse(hearingNotes)
    }

    private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> {
        return hearingDefendantRepository.findAllByDefendantCrn(crn)
    }

    private fun hearingNotesResponse(hearingNotes: List<HearingNoteEntity>): List<HearingNotesSarResponse> {
        return hearingNotes.filter {
            note -> !note.isDraft
        }.mapNotNull { note -> HearingNotesSarResponse(
            note.hearingId,
            note.note,
            note.author
        ) }
    }

    private fun getFilteredHearingNotes(hearingDefendants: List<HearingDefendantEntity>, fromDate: LocalDate?, toDate: LocalDate?): List<HearingNoteEntity> {
        return hearingDefendants.flatMap() {
            filterHearingNotesByDate(it, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))
        }.mapNotNull { it }
    }

    private fun filterHearingNotesByDate(hearingDefendant: HearingDefendantEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingNoteEntity> {
        if(fromDate != null && toDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBetween(
                hearingDefendant.id,
                fromDate,
                toDate
            )
        } else if(fromDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndCreatedAfter(
                hearingDefendant.id,
                fromDate
            )
        } else if(toDate != null) {
            return hearingNoteRepository.findAllByHearingDefendantIdAndCreatedBefore(
                hearingDefendant.id,
                toDate
            )
        }
        return hearingNoteRepository.findByHearingDefendantId(hearingDefendant.id)
    }
}