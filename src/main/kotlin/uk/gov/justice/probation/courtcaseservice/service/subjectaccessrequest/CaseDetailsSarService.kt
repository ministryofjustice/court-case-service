package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSarResponse
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

            if (getCase(cases, courtCase.urn) != null) {
                getCase(cases, courtCase.urn)?.hearings?.add(HearingSarResponse(hearing.hearingId, hearingNotes, hearingOutcomes))
            } else {
                val caseComments = defendantCaseCommentsService.getCaseCommentsForCaseDefendant(courtCase.urn, hearingDefendant, fromDate, toDate)
                val case = CaseSarResponse(courtCase.urn, mutableListOf(HearingSarResponse(hearing.hearingId, hearingNotes, hearingOutcomes)), caseComments)
                cases.add(case)
            }
        }
        // TODO: use findByCaseIdAndDefendantIdAndDeletedFalse to get case comments for each case
        // TODO: test with multiple court_cases
        // TODO: should we be returning deleted notes?
        // TODO: should the cases or hearings be sorted by date?
        // o each hearings have new defendant ids or does it share the same defendant id ?
        // TODO: if empty return null?
        // TODO: can case urn be null? As in a case has no urn
        // TODO: check if fk_hearing_defendant_id is on hearing_notes if not then no notes show up
        return cases
    }

    private fun getCase(cases: List<CaseSarResponse>, caseUrn: String): CaseSarResponse? {
        return cases.find { it.caseUrn == caseUrn }
    }

    private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> {
        return hearingDefendantRepository.findAllByDefendantCrn(crn)
    }
}