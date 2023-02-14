package uk.gov.justice.probation.courtcaseservice.service

data class PredictorParameters(
    var courtName: String,
    var offenderAge: Int? = null,
    var offenceCode: String

)



