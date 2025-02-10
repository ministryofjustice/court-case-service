package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.CasesSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import java.time.LocalDate
import java.time.LocalTime

@Service
class CaseDetailsSarService(val hearingDefendantRepository: HearingDefendantRepository,
                            val hearingOutcomesService: HearingOutcomesService,
                            val hearingNotesService: HearingNotesSARService,
                            val defendantCaseCommentsService: DefendantCaseCommentsService) {

    fun getCaseSARDetails(crn: String, fromDate: LocalDate, toDate: LocalDate): List<CasesSarResponse> {
        val hearingDefendants: List<HearingDefendantEntity> = getHearingDefendants(crn)

        val cases: List<CourtCaseEntity> = hearingDefendants.stream().map(HearingDefendantEntity::getHearing).map(HearingEntity::getCourtCase).toList()
//        val hearings: List<HearingEntity> = hearingDefendants.stream().map(HearingDefendantEntity::getHearing).toList()

        // case.hearings could include hearings this person didn't attend
        // hearing.hearingDefendants is the same issue, may not include him

        hearingDefendants.stream().map {
            hearingDefendant ->
            val hearingOutcomes = hearingOutcomesService.getHearingOutcomes(hearingDefendant, fromDate, toDate)
            val hearingNotes = hearingNotesService.getHearingNotes(hearingDefendant, fromDate, toDate)
            val caseComments = defendantCaseCommentsService.getCaseCommentsForDefendant(hearingDefendant, fromDate, toDate)
        }

        val hearingOutcomes = hearingOutcomesService.getHearingOutcomes(hearingDefendant, fromDate, toDate)



        return listOf(CasesSarResponse())
    }

    private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> {
        return hearingDefendantRepository.findAllByDefendantCrn(crn)
    }
}