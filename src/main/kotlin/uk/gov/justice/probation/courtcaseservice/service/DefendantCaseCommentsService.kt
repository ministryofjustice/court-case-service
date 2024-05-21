package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryFacade

@Service
class DefendantCaseCommentsService(
    private val caseCommentsService: CaseCommentsService,
    private val defendantRepositoryFacade: DefendantRepositoryFacade,
    private val immutableCourtCaseService: ImmutableCourtCaseService
) {
    fun getCaseCommentsForDefendant(crn: String): List<CaseCommentsSarResponse> {
        return defendantRepositoryFacade.findDefendantsByCrn(crn).stream().map {
            defendant ->
            caseCommentsSarResponses(defendant)
        }.toList().flatten()
    }

    private fun caseCommentsSarResponses(defendant: DefendantEntity): List<CaseCommentsSarResponse> =
        caseCommentsService.getCaseCommentsForDefendant(defendant.defendantId)
            .stream()
            .map { caseComment ->
                CaseCommentsSarResponse(
                    caseComment.comment,
                    caseComment.author,
                    caseComment.created,
                    caseComment.createdBy,
                    caseComment.lastUpdated,
                    caseComment.lastUpdatedBy,
                    getCaseNumber(caseComment)
                )
            }.toList()

    private fun getCaseNumber(caseComment: CaseCommentEntity): String {
        val findByCaseId: CourtCaseEntity? = immutableCourtCaseService.findByCaseId(caseComment.caseId).orElseGet(null)
        return if (findByCaseId?.sourceType == SourceType.LIBRA) findByCaseId.caseNo else ""
    }
}