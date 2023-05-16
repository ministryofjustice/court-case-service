package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDateTime

@Schema(description = "Hearing outcome response model")
class HearingOutcomeResponse(val hearingOutcomeType: HearingOutcomeType, val outcomeDate: LocalDateTime) {
    companion object {
        fun of(hearingOutcomeEntity: HearingOutcomeEntity?): HearingOutcomeResponse? =
            hearingOutcomeEntity?.let {
               return HearingOutcomeResponse(HearingOutcomeType.valueOf(it.outcomeType), it.outcomeDate)
           }?: null
    }

    fun getHearingOutcomeDescription(): String? = this.hearingOutcomeType?.value
}