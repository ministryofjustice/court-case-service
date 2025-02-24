package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import java.time.LocalDate

@Service
class CaseDetailsSarService(val hearingDefendantRepository: HearingDefendantRepository,
                            val hearingOutcomesService: HearingOutcomesService,
                            val hearingNotesService: HearingNotesSarService,
                            val defendantCaseCommentsService: DefendantCaseCommentsService) {

    fun getCaseSARDetails(crn: String, fromDate: LocalDate?, toDate: LocalDate?): List<CaseSarResponse> {
        val cases: MutableList<CaseSarResponse> = mutableListOf()

        getHearingDefendants(crn).map {
            hearingDefendant ->
            val hearing = hearingDefendant.hearing
            val courtCase = hearing.courtCase
            val hearingOutcomes = hearingOutcomesService.getHearingOutcomes(hearingDefendant, fromDate, toDate)
            val hearingNotes = hearingNotesService.getHearingNotes(hearingDefendant, fromDate, toDate)
            val hearingSarResponse = hearingSarResponse(hearing.hearingId, hearingNotes, hearingOutcomes)
            val caseComments = defendantCaseCommentsService.getCaseCommentsForDefendant(hearingDefendant, fromDate, toDate)

            val existingCase = getCase(cases, courtCase.urn)
            if (existingCase != null) {
                hearingSarResponse?.let { existingCase.hearings.add(it) }
            } else {
                val case = getCaseSarResponse(courtCase.urn, caseComments, hearingSarResponse)
                case?.let { cases.add(it) }
            }
        }
        return cases
    }

    private fun getCase(cases: List<CaseSarResponse>, caseUrn: String): CaseSarResponse? {
        return cases.find { it.urn == caseUrn }
    }

    private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> {
        return hearingDefendantRepository.findAllByDefendantCrn(crn)
    }

    private fun getCaseSarResponse(urn: String, caseComments: List<CaseCommentsSarResponse>, hearing: HearingSarResponse?): CaseSarResponse? {
        if (caseComments.isEmpty() && hearing == null) {
            return null
        } else if (hearing == null) {
            return CaseSarResponse(urn, mutableListOf(), caseComments)
        }
        return CaseSarResponse(urn, mutableListOf(hearing), caseComments)
    }

    private fun hearingSarResponse(hearingId: String, notes: List<HearingNotesSarResponse>, outcomes: List<HearingOutcomeSarResponse>): HearingSarResponse? {
        return if (notes.isEmpty() && outcomes.isEmpty()) {
            null
        } else {
            HearingSarResponse(hearingId, notes, outcomes)
        }
    }
}