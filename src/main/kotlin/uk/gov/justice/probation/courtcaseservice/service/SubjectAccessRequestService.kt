package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService: HmppsProbationSubjectAccessRequestService {
    override fun getProbationContentFor(
        crn: String,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): HmppsSubjectAccessRequestContent? {
        return HmppsSubjectAccessRequestContent("stuff")
    }
}