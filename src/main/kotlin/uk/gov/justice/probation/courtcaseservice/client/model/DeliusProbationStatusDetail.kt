package uk.gov.justice.probation.courtcaseservice.client.model

import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail
import java.time.LocalDate

data class DeliusProbationStatusDetail(
    val status: ProbationStatus,
    val terminationDate: LocalDate? = null,
    val inBreach: Boolean = false,
    val preSentenceActivity: Boolean = false,
    val awaitingPsr: Boolean = false
) {
    companion object {

        val NO_RECORD = DeliusProbationStatusDetail(ProbationStatus.NO_RECORD)

        fun from(probationStatusDetail: DeliusProbationStatusDetail): ProbationStatusDetail {

            return ProbationStatusDetail.builder()
                .status(probationStatusDetail.status.name)
                .awaitingPsr(probationStatusDetail.awaitingPsr)
                .inBreach(probationStatusDetail.inBreach)
                .preSentenceActivity(probationStatusDetail.preSentenceActivity)
                .previouslyKnownTerminationDate(probationStatusDetail.terminationDate)
                .build();
        }
    }
}

enum class ProbationStatus {
    NO_RECORD,
    NOT_SENTENCED,
    PREVIOUSLY_KNOWN,
    CURRENT
}
