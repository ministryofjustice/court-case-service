package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class HearingOutcomeItemState(@JsonProperty("hearingOutcomeState") val value: String) {
    NEW("New"), IN_PROGRESS("In progress"), RESULTED("Resulted")
}