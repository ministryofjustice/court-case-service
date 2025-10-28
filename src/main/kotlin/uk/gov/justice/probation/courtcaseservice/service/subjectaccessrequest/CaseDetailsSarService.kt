package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.AddressSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.JudicialResultSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumberSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.PleaSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.VerdictSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import java.time.LocalDate

@Service
class CaseDetailsSarService(
  val hearingDefendantRepository: HearingDefendantRepository,
  val hearingOutcomesService: HearingOutcomesService,
  val hearingNotesService: HearingNotesSarService,
  val defendantCaseCommentsService: DefendantCaseCommentsService,
) {

  fun getCaseSARDetails(crn: String, fromDate: LocalDate?, toDate: LocalDate?): List<CaseSarResponse> {
    val cases: MutableList<CaseSarResponse> = mutableListOf()

    getHearingDefendants(crn).map { hearingDefendant ->
      val hearing = hearingDefendant.hearing
      val courtCase = hearing.courtCase
      val hearingOutcomes = hearingOutcomesService.getHearingOutcomes(hearingDefendant, fromDate, toDate)
      val hearingNotes = hearingNotesService.getHearingNotes(hearingDefendant, fromDate, toDate)
      val defendant = getDefendantSarResponse(hearingDefendant.defendant)
      val offences = getOffenceSarResponses(hearingDefendant.offences)
      val hearingSarResponse = hearingSarResponse(hearing.hearingId, hearing.hearingEventType.name, hearingNotes, hearingOutcomes, defendant, offences)
      val caseComments = defendantCaseCommentsService.getCaseCommentsForDefendant(hearingDefendant, fromDate, toDate)

      val existingCase = getCase(cases, courtCase.caseId)
      if (existingCase != null) {
        hearingSarResponse.let { existingCase.hearings.add(it) }
      } else {
        val case = getCaseSarResponse(courtCase.caseId, caseComments, hearingSarResponse)
        case.let { cases.add(it) }
      }
    }
    return cases
  }

  private fun getCase(cases: List<CaseSarResponse>, caseId: String): CaseSarResponse? = cases.find { it.caseId == caseId }

  private fun getHearingDefendants(crn: String): List<HearingDefendantEntity> = hearingDefendantRepository.findAllByDefendantCrn(crn)

  private fun getCaseSarResponse(urn: String, caseComments: List<CaseCommentsSarResponse>, hearing: HearingSarResponse?): CaseSarResponse {
    if (hearing == null) {
      return CaseSarResponse(urn, mutableListOf(), caseComments)
    }
    return CaseSarResponse(urn, mutableListOf(hearing), caseComments)
  }

  private fun getOffenceSarResponses(offenses: List<OffenceEntity>): List<OffenceSarResponse> = offenses.map { offenceEntity -> getOffenceSarResponse(offenceEntity) }

  private fun getDefendantSarResponse(defendant: DefendantEntity): DefendantSarResponse = DefendantSarResponse(
    defendant.crn,
    defendant.defendantName,
    defendant.type,
    AddressSarResponse(
      defendant.address.line1,
      defendant.address.line2,
      defendant.address.line3,
      defendant.address.line4,
      defendant.address.line5,
      defendant.address.postcode,
    ),
    defendant.pnc,
    defendant.cro,
    defendant.dateOfBirth,
    defendant.sex.name,
    defendant.nationality1,
    defendant.nationality2,
    defendant.isManualUpdate,
    defendant.isOffenderConfirmed,
    PhoneNumberSarResponse(
      defendant.phoneNumber?.home,
      defendant.phoneNumber?.mobile,
      defendant.phoneNumber?.work,
    ),
  )

  private fun getOffenceSarResponse(offence: OffenceEntity): OffenceSarResponse = OffenceSarResponse(
    offence.title,
    offence.summary,
    offence.act,
    offence.sequence,
    offence.listNo,
    offence.offenceCode,
    PleaSarResponse(
      offence.plea?.value,
      offence.plea?.date,
    ),
    VerdictSarResponse(
      offence.verdict?.typeDescription,
      offence.verdict?.date,
    ),
    offence.judicialResults.map {
      judicialResultEntity ->
      JudicialResultSarResponse(
        judicialResultEntity?.label,
        judicialResultEntity?.isConvictedResult,
        judicialResultEntity?.judicialResultTypeId,
        judicialResultEntity?.resultText,
      )
    },
  )

  private fun hearingSarResponse(
    hearingId: String,
    hearingEventType: String,
    notes: List<HearingNotesSarResponse>,
    outcomes: List<HearingOutcomeSarResponse>,
    defendant: DefendantSarResponse,
    offences: List<OffenceSarResponse>,
  ): HearingSarResponse = HearingSarResponse(hearingId, hearingEventType, notes, outcomes, defendant, offences)
}
