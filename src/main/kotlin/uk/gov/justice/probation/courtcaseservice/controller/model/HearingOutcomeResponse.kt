package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
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
    val state: HearingOutcomeItemState? = null,
    val legacy: Boolean? = false
) {
    companion object {
        fun of(hearingOutcomeEntity: HearingOutcomeEntity?): HearingOutcomeResponse? =
            hearingOutcomeEntity?.let {
                return HearingOutcomeResponse(
                    hearingOutcomeType = HearingOutcomeType.valueOf(it.outcomeType),
                    outcomeDate = it.outcomeDate,
                    state = HearingOutcomeItemState.valueOf(it.state),
                    legacy = it.isLegacy
                )
            } ?: null

        fun of(defendantEntity: HearingDefendantEntity, hearingDate: LocalDate): HearingOutcomeResponse {
            val hearingOutcomeEntity = defendantEntity.hearingOutcome
            return HearingOutcomeResponse(
                hearingOutcomeType = HearingOutcomeType.valueOf(hearingOutcomeEntity.outcomeType),
                outcomeDate = hearingOutcomeEntity.outcomeDate,
                resultedDate = hearingOutcomeEntity.resultedDate,
                hearingDate = hearingDate,
                hearingId = defendantEntity.hearing.hearingId,
                defendantId = defendantEntity.defendantId,
                probationStatus = defendantEntity.defendant.probationStatusForDisplay.getName(),
                offences = defendantEntity.offences.map { offenceEntity -> offenceEntity.title },
                defendantName = defendantEntity.defendant.defendantName,
                crn = defendantEntity.defendant?.offender?.crn,
                assignedTo = hearingOutcomeEntity.assignedTo,
                assignedToUuid = hearingOutcomeEntity.assignedToUuid,
                state = HearingOutcomeItemState.valueOf(hearingOutcomeEntity.state),
                legacy = hearingOutcomeEntity.isLegacy
            )
        }

        fun of(defendantDTO: HearingDefendantDTO, hearingDate: LocalDate): HearingOutcomeResponse {
            val hearingOutcomeEntity = defendantDTO.hearingOutcome
            return HearingOutcomeResponse(
                hearingOutcomeType = HearingOutcomeType.valueOf(hearingOutcomeEntity.outcomeType),
                outcomeDate = hearingOutcomeEntity.outcomeDate,
                resultedDate = hearingOutcomeEntity.resultedDate,
                hearingDate = hearingDate,
                hearingId = defendantDTO.hearing.hearingId,
                defendantId = defendantDTO.defendantId,
                probationStatus = defendantDTO.defendant.probationStatusForDisplay.getName(),
                offences = defendantDTO.offences.map { offenceEntity -> offenceEntity.title },
                defendantName = defendantDTO.defendant.defendantName,
                crn = defendantDTO.defendant?.offender?.crn,
                assignedTo = hearingOutcomeEntity.assignedTo,
                assignedToUuid = hearingOutcomeEntity.assignedToUuid,
                state = HearingOutcomeItemState.valueOf(hearingOutcomeEntity.state),
                legacy = hearingOutcomeEntity.isLegacy
            )
        }
    }

    fun getHearingOutcomeDescription(): String = this.hearingOutcomeType.value
}