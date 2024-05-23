package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class HearingOutcomesSARService(
    val hearingDefendantRepository: HearingDefendantRepository,
    val hearingOutcomeRepository: HearingOutcomeRepository
) {

    fun getHearingOutcomes(crn: String,fromDate: LocalDate?,toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        val hearingDefendants: List<HearingDefendantEntity> = hearingDefendantRepository.findAllByDefendantCrn(crn)
        return hearingOutcomesResponse(hearingDefendants, fromDate, toDate)

    }

    private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> {
        return hearingDefendantRepository.findAllByDefendantCrn(crn)
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
            filteredHearingOutcomesByDate(it, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))
        }.mapNotNull { it }
    }

    private fun filteredHearingOutcomesByDate(hearingDefendant: HearingDefendantEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingOutcomeEntity> {
        if(fromDate != null && toDate != null) {
            return hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBetween(
                hearingDefendant.id,
                fromDate,
                toDate
            )
        } else if(fromDate != null) {
            return hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedAfter(
                hearingDefendant.id,
                fromDate
            )
        } else if(toDate != null) {
            return hearingOutcomeRepository.findAllByHearingDefendantIdAndCreatedBefore(
                hearingDefendant.id,
                toDate
            )
        }
        return hearingOutcomeRepository.findByHearingDefendantId(hearingDefendant.id)
    }
}