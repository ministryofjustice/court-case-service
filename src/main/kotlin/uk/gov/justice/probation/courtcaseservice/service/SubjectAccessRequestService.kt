package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import uk.gov.justice.probation.courtcaseservice.controller.model.ContentSarResponse
import java.time.LocalDate

@Service
class SubjectAccessRequestService(private val defendantCaseCommentsService: DefendantCaseCommentsService): HmppsProbationSubjectAccessRequestService {
    override fun getProbationContentFor(
        crn: String,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): HmppsSubjectAccessRequestContent? {
        return HmppsSubjectAccessRequestContent(ContentSarResponse(defendantCaseCommentsService.getCaseCommentsForDefendant(crn, fromDate, toDate)))
    }
}