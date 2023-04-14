package uk.gov.justice.probation.courtcaseservice.service

enum class HearingOutcomeType(val value: String) {

    PROBATION_SENTENCE("Probation sentence"),
    NON_PROBATION_SENTENCE("Non-probation sentence"),
    REPORT_REQUESTED("Report requested"),
    ADJOURNED("Adjourned"),
    COMMITTED_TO_CROWN("Committed to Crown"),
    CROWN_PLUS_PSR("Crown plus PSR"),
    OTHER("Other");
}