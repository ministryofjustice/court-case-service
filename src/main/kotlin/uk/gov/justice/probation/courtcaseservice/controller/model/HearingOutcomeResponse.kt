package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Hearing outcome response model")
data class HearingOutcomeResponse(
    val hearingOutcomeType: HearingOutcomeType,
    val outcomeDate: LocalDateTime,
    val resultedDate: LocalDateTime? = null,
    val hearingDate: LocalDate? = null,
    val hearingId: String? = null,
    val defendantId: String? = null,
    val probationStatus: String? = null,
    val offences: List<String>? = null,
    val defendantName: String? = null,
    val crn: String? = null,
    val assignedTo: String? = null,
    val assignedToUuid: String? = null,
    val state: HearingOutcomeItemState? = null
) {
    companion object {
        fun of(hearingOutcomeEntity: HearingOutcomeEntity?): HearingOutcomeResponse? =
            hearingOutcomeEntity?.let {
                return HearingOutcomeResponse(
                    hearingOutcomeType = HearingOutcomeType.valueOf(it.outcomeType),
                    outcomeDate = it.outcomeDate,
                    state = HearingOutcomeItemState.valueOf(it.state)
                )
            } ?: null

        fun of(hearing: HearingEntity, hearingDate: LocalDate): List<HearingOutcomeResponse> = hearing.hearingDefendants.map { hd ->
            HearingOutcomeResponse(
                hearingOutcomeType = HearingOutcomeType.valueOf(hearing.hearingOutcome.outcomeType),
                outcomeDate = hearing.hearingOutcome.outcomeDate,
                resultedDate = hearing.hearingOutcome.resultedDate,
                hearingDate = hearingDate,
                hearingId = hearing.hearingId,
                defendantId = hd.defendantId,
                probationStatus = hd.defendant.probationStatusForDisplay.getName(),
                offences = hd.offences.map { offenceEntity -> offenceEntity.title },
                defendantName = hd.defendant.defendantName,
                crn = hd.defendant?.offender?.crn,
                assignedTo = hearing.hearingOutcome.assignedTo,
                assignedToUuid = hearing.hearingOutcome.assignedToUuid,
                state = HearingOutcomeItemState.valueOf(hearing.hearingOutcome.state)
            )
        }
    }

    fun getHearingOutcomeDescription(): String? = this.hearingOutcomeType?.value
}