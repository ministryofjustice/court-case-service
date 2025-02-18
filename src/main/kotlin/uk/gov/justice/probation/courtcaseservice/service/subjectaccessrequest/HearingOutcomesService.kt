package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.chrono.ChronoLocalDateTime

@Service
class HearingOutcomesService {

    fun getHearingOutcomes(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?,toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        return hearingOutcomesResponse(hearingDefendant, fromDate, toDate)
    }

    private fun hearingOutcomesResponse(hearingDefendant: HearingDefendantEntity, fromDate: LocalDate?, toDate: LocalDate?): List<HearingOutcomeSarResponse> {
        return filteredHearingOutcomesByDate(hearingDefendant, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX)).map {
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

    private fun getSurname(name: String): String {
        return name.split(" ").last()
    }

    private fun filteredHearingOutcomesByDate(hearingDefendant: HearingDefendantEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingOutcomeEntity> {
        val outcome = hearingDefendant.hearingOutcome ?: return emptyList()
        if (outcome.isDeleted || outcome.isLegacy) {
            return emptyList()
        }
        if(fromDate != null && toDate != null) {
            return outcomes(outcome, fromDate, toDate)
        } else if(fromDate != null) {
            return outcomesAfter(outcome, fromDate)
        } else if(toDate != null) {
            return outcomesBefore(outcome, toDate)
        }
        return listOf(outcome)
    }

    private fun outcomes(outcome: HearingOutcomeEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingOutcomeEntity> {
        if (outcome.created >= ChronoLocalDateTime.from(fromDate) && outcome.created <= ChronoLocalDateTime.from(toDate)) {
            return listOf(outcome)
        }
        return emptyList()
    }

    private fun outcomesAfter(outcome: HearingOutcomeEntity, fromDate: LocalDateTime?): List<HearingOutcomeEntity> {
        if (outcome.created >= ChronoLocalDateTime.from(fromDate)) {
            return listOf(outcome)
        }
        return emptyList()
    }

    private fun outcomesBefore(outcome: HearingOutcomeEntity, toDate: LocalDateTime?): List<HearingOutcomeEntity>  {
        if (outcome.created <= ChronoLocalDateTime.from(toDate)){
            return listOf(outcome)
        }
        return emptyList()
    }
}