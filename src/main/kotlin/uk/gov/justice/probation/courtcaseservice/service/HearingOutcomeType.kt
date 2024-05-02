package uk.gov.justice.probation.courtcaseservice.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema


@Schema(implementation = HearingOutcomeType::class)
enum class HearingOutcomeType(@JsonProperty("hearingOutcomeType") val value: String) {

    PROBATION_SENTENCE("Probation sentence"),
    NON_PROBATION_SENTENCE("Non-probation sentence"),
    REPORT_REQUESTED("Report requested"),
    ADJOURNED("Adjourned"),
    COMMITTED_TO_CROWN("Committed to Crown"),
    CROWN_PLUS_PSR("Crown plus PSR"),
    NO_OUTCOME("No outcome"),
    OTHER("Other"),
    WARRANT("Warrant"),
    TRIAL("Trial");
}