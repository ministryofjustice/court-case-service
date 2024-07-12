package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryFacade
import java.time.LocalDate
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService
import uk.gov.justice.probation.courtcaseservice.service.ImmutableCourtCaseService

@Service
class DefendantCaseCommentsService(
    private val caseCommentsService: CaseCommentsService,
    private val defendantRepositoryFacade: DefendantRepositoryFacade,
    private val immutableCourtCaseService: ImmutableCourtCaseService
) {
    fun getCaseCommentsForDefendant(crn: String, fromDate: LocalDate?, toDate: LocalDate?): List<CaseCommentsSarResponse> {
        return defendantRepositoryFacade.findDefendantsByCrn(crn).stream().map {
            defendant ->
            caseCommentsSarResponses(findDefendantsByCrnAndDateRange(defendant.defendantId, fromDate, toDate))
        }.toList().flatten()
    }

    private fun caseCommentsSarResponses(caseCommentEntities: List<CaseCommentEntity>): List<CaseCommentsSarResponse> =
        caseCommentEntities
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
        val findByCaseId: CourtCaseEntity? = immutableCourtCaseService.findByCaseId(caseComment.caseId).orElse(null)
        return if (findByCaseId?.sourceType == SourceType.LIBRA) findByCaseId.caseNo else ""
    }

    private fun findDefendantsByCrnAndDateRange(defendantId: String, fromDate: LocalDate?, toDate: LocalDate?): List<CaseCommentEntity> {
        if(fromDate != null && toDate != null) {
            return caseCommentsService.getCaseCommentsForDefendantBetween(defendantId, fromDate, toDate)
        } else if(fromDate != null) {
            return caseCommentsService.getCaseCommentsForDefendantFrom(defendantId, fromDate)
        } else if(toDate != null) {
            return caseCommentsService.getCaseCommentsForDefendantTo(defendantId, toDate)
        }
        return caseCommentsService.getCaseCommentsForDefendant(defendantId)
    }
}