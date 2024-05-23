package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import java.time.LocalDate
import java.time.LocalTime

@Service
class HearingDefendantsService(
    val hearingDefendantRepository: HearingDefendantRepository,
    val hearingOutcomeRepository: HearingOutcomeRepository,
    val hearingNoteRepository: HearingNoteRepository
) {

    fun getHearingOutcomes(crn: String,fromDate: LocalDate?,toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        val hearingDefendants: List<HearingDefendantEntity> = hearingDefendantRepository.findAllByDefendantCrn(crn)
        return hearingOutcomesResponse(hearingDefendants, fromDate, toDate)

    }

    fun getHearingNotes(crn: String,fromDate: LocalDate?,toDate: LocalDate?): List<HearingNotesSarResponse> {
        val hearingNotes = getHearingDefendants(crn).flatMap {
            hearingNoteRepository.findAllByHearingDefendantIdAndCreatedIsBetween(it.id, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))
        }

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

    private fun hearingOutcomesResponse(hearingDefendants: List<HearingDefendantEntity>, fromDate: LocalDate?, toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        return getFilteredHearingOutcomes(hearingDefendants, fromDate, toDate).map {
                hearingOutcome ->
            HearingOutcomeSarResponse(
                hearingOutcome.outcomeType,
                hearingOutcome.outcomeDate,
                hearingOutcome.resultedDate,
                hearingOutcome.state,
                hearingOutcome.assignedTo,
                hearingOutcome.created
            )
        }
    }
    private fun getFilteredHearingOutcomes(hearingDefendants: List<HearingDefendantEntity>, fromDate: LocalDate?, toDate: LocalDate?): List<HearingOutcomeEntity> {
        return hearingDefendants.flatMap() {
            hearingOutcomeRepository.findAllByHearingDefendantIdAndOutcomeDateBetween(
                it.id,
                fromDate?.atStartOfDay(),
                toDate?.atTime(LocalTime.MAX)
            )
        }.mapNotNull { it }
    }
}