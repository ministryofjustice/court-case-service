package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class HearingOutcomesService(
    val hearingOutcomeRepository: HearingOutcomeRepository
) {

    fun getHearingOutcomes(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?,toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        return hearingOutcomesResponse(hearingDefendant, fromDate, toDate)
    }

    private fun hearingOutcomesResponse(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?, toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        return getFilteredHearingOutcomes(hearingDefendant, fromDate, toDate).map {
                hearingOutcome ->
            HearingOutcomeSarResponse(
                HearingOutcomeType.valueOf(hearingOutcome.outcomeType).value,
                hearingOutcome.outcomeDate,
                hearingOutcome.resultedDate,
                HearingOutcomeItemState.valueOf(hearingOutcome.state).value,
                getSurname(hearingOutcome.assignedTo),
                hearingOutcome.created
            )
        }
    }
    private fun getFilteredHearingOutcomes(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?, toDate: LocalDate?): List<HearingOutcomeEntity> {
        return filteredHearingOutcomesByDate(hearingDefendant, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))
    }

    private fun getSurname(name: String): String {
        return name.split(" ").last()
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