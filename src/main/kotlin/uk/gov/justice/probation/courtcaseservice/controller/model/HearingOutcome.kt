package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

@Schema(description = "Hearing outcome model")
data class HearingOutcome(val hearingOutcomeType: HearingOutcomeType) {

    companion object {
        fun of(hearingOutcomeEntity: HearingOutcomeEntity?): HearingOutcome? =
            hearingOutcomeEntity?.let {
                HearingOutcome(
                    HearingOutcomeType.valueOf(
                        hearingOutcomeEntity.outcomeType
                    )
                )
            }?: null
    }
}
