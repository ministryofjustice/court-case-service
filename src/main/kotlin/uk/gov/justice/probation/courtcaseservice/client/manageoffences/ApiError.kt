package uk.gov.justice.probation.courtcaseservice.client.manageoffences

data class ApiError(
    val status: Int,
    val errorCode: String? = null,
    val userMessage: String,
    val developerMessage: String,
    val moreInfo: String? = null
)
