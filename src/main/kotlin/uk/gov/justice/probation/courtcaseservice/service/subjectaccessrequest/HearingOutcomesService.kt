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
        filteredHearingOutcomesByDate(hearingDefendant, fromDate?.atStartOfDay(), toDate?.atTime(LocalTime.MAX))?.let {
            return listOf(
                HearingOutcomeSarResponse(
                    HearingOutcomeType.valueOf(it.outcomeType).value,
                    it.outcomeDate,
                    it.resultedDate,
                    HearingOutcomeItemState.valueOf(it.state).value,
                    getSurname(it.assignedTo),
                    it.created
                )
            )
        }
        return emptyList()
    }

    private fun getSurname(name: String): String {
        return name.split(" ").last()
    }

    private fun filteredHearingOutcomesByDate(hearingDefendant: HearingDefendantEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): HearingOutcomeEntity? {
        val outcome = hearingDefendant.hearingOutcome ?: return null
        if (outcome.isDeleted || outcome.isLegacy) {
            return null
        }
        if(fromDate != null && toDate != null) {
            return outcomeBetweenDates(outcome, fromDate, toDate)
        } else if(fromDate != null) {
            return outcomeAfterDate(outcome, fromDate)
        } else if(toDate != null) {
            return outcomeBeforeDate(outcome, toDate)
        }
        return outcome
    }

    private fun outcomeBetweenDates(outcome: HearingOutcomeEntity, fromDate: LocalDateTime?, toDate: LocalDateTime?): HearingOutcomeEntity? {
        if (outcome.created >= ChronoLocalDateTime.from(fromDate) && outcome.created <= ChronoLocalDateTime.from(toDate)) {
            return outcome
        }
        return null
    }

    private fun outcomeAfterDate(outcome: HearingOutcomeEntity, fromDate: LocalDateTime?): HearingOutcomeEntity? {
        if (outcome.created >= ChronoLocalDateTime.from(fromDate)) {
            return outcome
        }
        return null
    }

    private fun outcomeBeforeDate(outcome: HearingOutcomeEntity, toDate: LocalDateTime?): HearingOutcomeEntity?  {
        if (outcome.created <= ChronoLocalDateTime.from(toDate)){
            return outcome
        }
        return null
    }
}