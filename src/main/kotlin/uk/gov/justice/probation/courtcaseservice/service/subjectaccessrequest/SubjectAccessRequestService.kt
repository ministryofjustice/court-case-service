package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import uk.gov.justice.probation.courtcaseservice.controller.model.ContentSarResponse
import java.time.LocalDate

@Service
class SubjectAccessRequestService(
    private val defendantCaseCommentsService: DefendantCaseCommentsService, val hearingDefendantsService: HearingDefendantsService
): HmppsProbationSubjectAccessRequestService {

    override fun getProbationContentFor(
        crn: String,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): HmppsSubjectAccessRequestContent? {
        val hearingOutcomes = hearingDefendantsService.getHearingOutcomes(crn, fromDate, toDate)
        val hearingNotes = hearingDefendantsService.getHearingNotes(crn, fromDate, toDate)

        return HmppsSubjectAccessRequestContent(ContentSarResponse(defendantCaseCommentsService.getCaseCommentsForDefendant(crn, fromDate, toDate)), hearingOutcomes, hearingNotes))
    }
}