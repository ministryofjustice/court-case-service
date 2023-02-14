package uk.gov.justice.probation.courtcaseservice.service

data class ShortTermCustodyPredictorParameters(
    var courtName: String,
    var offenderAge: Int? = null,
    var offenceCode: String

)



