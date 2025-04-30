package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import uk.gov.justice.probation.courtcaseservice.controller.model.ContentSarResponse
import java.time.LocalDate

@Service
class SubjectAccessRequestService(
  private val caseDetailsSarService: CaseDetailsSarService,
) : HmppsProbationSubjectAccessRequestService {

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val cases = caseDetailsSarService.getCaseSARDetails(crn, fromDate, toDate)

    if (cases.isEmpty()) {
      return null
    }

    return HmppsSubjectAccessRequestContent(ContentSarResponse(cases))
  }
}
