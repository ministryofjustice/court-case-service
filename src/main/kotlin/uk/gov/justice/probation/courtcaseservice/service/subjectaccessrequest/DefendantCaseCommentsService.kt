package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import java.time.LocalDate
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService
import uk.gov.justice.probation.courtcaseservice.service.ImmutableCourtCaseService

@Service
class DefendantCaseCommentsService(
    private val caseCommentsService: CaseCommentsService,
    private val immutableCourtCaseService: ImmutableCourtCaseService
) {
    fun getCaseCommentsForDefendant(hearingDefendantEntity: HearingDefendantEntity, fromDate: LocalDate?, toDate: LocalDate?): List<CaseCommentsSarResponse> {
        return caseCommentsSarResponses(findDefendantsByCrnAndDateRange(hearingDefendantEntity.defendantId, fromDate, toDate))
    }

    private fun caseCommentsSarResponses(caseCommentEntities: List<CaseCommentEntity>): List<CaseCommentsSarResponse> =
        caseCommentEntities
            .map { caseComment ->
                if (caseComment.isDraft || caseComment.isDeleted || caseComment.isLegacy){
                    return@map null
                }
                CaseCommentsSarResponse(
                    caseComment.comment,
                    getSurname(caseComment.author),
                    caseComment.created,
                    caseComment.lastUpdated,
                    getLastUpdatedBy(caseComment.lastUpdatedBy),
                    getCaseNumber(caseComment)
                )
            }.filterNotNull()

    private fun getSurname(name: String): String {
        return name.split(" ").last()
    }

    private fun getLastUpdatedBy(name: String): String {
        return name.split("(").first()
    }

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