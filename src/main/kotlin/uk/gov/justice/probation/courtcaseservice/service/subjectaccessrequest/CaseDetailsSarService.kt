package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CaseDetailsSarService(
  val hearingDefendantRepository: HearingDefendantRepository,
  val hearingOutcomesService: HearingOutcomesService,
  val hearingNotesService: HearingNotesSarService,
  val defendantCaseCommentsService: DefendantCaseCommentsService,
) : ISarFormatter {

  fun getCaseSARDetails(crn: String, fromDate: LocalDate?, toDate: LocalDate?): List<CaseSarResponse> {
    val cases: MutableList<CaseSarResponse> = mutableListOf()

    getHearingDefendants(crn).map { hearingDefendant ->
      val hearing = hearingDefendant.hearing
      val courtCase = hearing.courtCase
      val hearingOutcomes = hearingOutcomesService.getHearingOutcomes(hearingDefendant, fromDate, toDate)
      val hearingNotes = hearingNotesService.getHearingNotes(hearingDefendant, fromDate, toDate)
      val hearingSarResponse = hearingSarResponse(hearing.hearingId, hearing.hearingEventType?.name, hearingNotes, hearingOutcomes)
      val caseComments = defendantCaseCommentsService.getCaseCommentsForDefendant(hearingDefendant, fromDate, toDate)

      val existingCase = getCase(cases, courtCase.caseId)
      if (existingCase != null) {
        hearingSarResponse.let { existingCase.hearings.add(it) }
      } else {
        val case = getCaseSarResponse(
          urn = courtCase.caseId,
          caseNo = courtCase.caseNo ?: "",
          created = courtCase.created,
          lastUpdated = courtCase.lastUpdated ?: courtCase.created,
          createdBy = getCreatedBy(courtCase.createdBy),
          lastUpdatedBy = getLastUpdatedBy(courtCase.lastUpdatedBy),
          caseComments = caseComments,
          hearing = hearingSarResponse,
        )
        case.let { cases.add(it) }
      }
    }
    return cases
  }

  private fun getCase(cases: List<CaseSarResponse>, caseId: String): CaseSarResponse? = cases.find { it.caseId == caseId }

  private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> = hearingDefendantRepository.findAllByDefendantCrn(crn)

  private fun getCaseSarResponse(
    urn: String,
    caseNo: String,
    created: LocalDateTime?,
    lastUpdated: LocalDateTime?,
    createdBy: String?,
    lastUpdatedBy: String?,
    caseComments: List<CaseCommentsSarResponse>,
    hearing: HearingSarResponse?,
  ): CaseSarResponse {
    if (hearing == null) {
      return CaseSarResponse(
        caseId = urn,
        caseNo,
        created,
        lastUpdated,
        createdBy = createdBy ?: "",
        lastUpdatedBy = lastUpdatedBy ?: "",
        hearings = mutableListOf(),
        comments = caseComments,
      )
    }
    return CaseSarResponse(
      caseId = urn,
      caseNo,
      created,
      lastUpdated = lastUpdated ?: created,
      createdBy = createdBy ?: "",
      lastUpdatedBy = lastUpdatedBy ?: "",
      hearings = mutableListOf(hearing),
      comments = caseComments,
    )
  }

  private fun hearingSarResponse(
    hearingId: String,
    hearingEventType: String?,
    notes: List<HearingNotesSarResponse>,
    outcomes: List<HearingOutcomeSarResponse>,
  ): HearingSarResponse = HearingSarResponse(hearingId, notes, outcomes)
}
