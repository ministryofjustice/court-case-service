package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentsSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNotesSarResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSarResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest.CaseDetailsSarService
import uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest.DefendantCaseCommentsService
import uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest.HearingNotesSarService
import uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest.HearingOutcomesService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class CaseDetailsSarServiceTest {

  @Mock
  lateinit var hearingDefendantRepository: HearingDefendantRepository

  @Mock
  lateinit var hearingOutcomesService: HearingOutcomesService

  @Mock
  lateinit var hearingNotesService: HearingNotesSarService

  @Mock
  lateinit var defendantCaseCommentsService: DefendantCaseCommentsService

  private lateinit var caseDetailsSarService: CaseDetailsSarService

  private val hearing: HearingEntity = HearingEntity.builder()
    .hearingId("uuid")
    .hearingEventType(HearingEventType.RESULTED)
    .courtCase(aCourtCaseEntity())
    .build()
  private val hearingDefendant: HearingDefendantEntity =
    HearingDefendantEntity.builder().defendantId("uuid").defendant(aDefendantEntity("uuid", "X340906"))
      .hearingOutcome(aHearingOutcomeEntity())
      .hearing(hearing)
      .build()
  private val courtCase2: CourtCaseEntity = CourtCaseEntity.builder().id(2).caseId("caseId2").caseId("CASEID2").build()
  private val hearing2: HearingEntity = HearingEntity.builder().id(2)
      .hearingId("uuid2")
      .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
      .courtCase(courtCase2)
    .build()
  private val hearingDefendant2: HearingDefendantEntity =
    HearingDefendantEntity.builder().id(2).defendantId("uuid").defendant(aDefendantEntity("uuid", "X340906"))
      .hearingOutcome(aHearingOutcomeEntity())
      .hearing(hearing2)
      .build()

  @BeforeEach
  fun beforeEach() {
    caseDetailsSarService = CaseDetailsSarService(
      hearingDefendantRepository,
      hearingOutcomesService,
      hearingNotesService,
      defendantCaseCommentsService,
    )
  }

  @Test
  fun `given defendant CRN and no dates, should return list of known cases in CaseSarResponse`() {
    whenever(hearingDefendantRepository.findAllByDefendantCrn("X340906")).thenReturn(listOf(hearingDefendant))
    whenever(hearingNotesService.getHearingNotes(hearingDefendant, null, null)).thenReturn(
      listOf(
        HearingNotesSarResponse("hearing note", "author"),
      ),
    )
    whenever(defendantCaseCommentsService.getCaseCommentsForDefendant(hearingDefendant, null, null)).thenReturn(
      listOf(CaseCommentsSarResponse("Some comment", "Author", LocalDateTime.parse("2024-05-22T12:00"), LocalDateTime.parse("2024-05-22T12:30"), "TestUser", "")),
    )
    whenever(hearingOutcomesService.getHearingOutcomes(hearingDefendant, null, null)).thenReturn(
      listOf(
        HearingOutcomeSarResponse(
          "Adjourned",
          LocalDateTime.parse("2020-05-01T00:00"),
          LocalDateTime.parse("2020-05-01T00:00"),
          "In progress",
          "Doe",
          LocalDateTime.parse("2024-01-01T00:00"),
        ),
      ),
    )
    val caseSARDetails = caseDetailsSarService.getCaseSARDetails("X340906", null, null)

    assertThat(caseSARDetails).hasSize(1)
    assertThat(caseSARDetails[0].caseId).isEqualTo("5678")
    assertThat(caseSARDetails[0].comments).hasSize(1)
    assertThat(caseSARDetails[0].comments[0].comment).isEqualTo("Some comment")
    assertThat(caseSARDetails[0].hearings).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].hearingId).isEqualTo("uuid")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[0].hearings[0].notes).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].notes[0].note).isEqualTo("hearing note")
    assertThat(caseSARDetails[0].hearings[0].notes[0].authorSurname).isEqualTo("author")
    assertThat(caseSARDetails[0].hearings[0].outcomes).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].outcomes[0].outcomeType).isEqualTo("Adjourned")
    assertThat(caseSARDetails[0].hearings[0].outcomes[0].outcomeDate).isEqualTo(LocalDateTime.parse("2020-05-01T00:00"))
  }

  @Test
  fun `given unknown defendant CRN and no dates, should return empty list`() {
    whenever(hearingDefendantRepository.findAllByDefendantCrn("X340906")).thenReturn(listOf())

    val caseSARDetails = caseDetailsSarService.getCaseSARDetails("X340906", null, null)

    assertThat(caseSARDetails).hasSize(0)
  }

  @Test
  fun `given defendant crn with multiple hearings in the same case, should return 1 case with 2 hearingSarResponse in CaseSarResponse`() {
    hearing2.courtCase = hearing.courtCase

    whenever(hearingDefendantRepository.findAllByDefendantCrn("X340906")).thenReturn(
      listOf(
        hearingDefendant,
        hearingDefendant2,
      ),
    )
    whenever(hearingNotesService.getHearingNotes(hearingDefendant, null, null)).thenReturn(
      listOf(
        HearingNotesSarResponse("hearing 1 note", "author1"),
      ),
    )
    whenever(hearingNotesService.getHearingNotes(hearingDefendant2, null, null)).thenReturn(
      listOf(
        HearingNotesSarResponse("hearing 2 note", "author2"),
      ),
    )
    val caseSARDetails = caseDetailsSarService.getCaseSARDetails("X340906", null, null)

    assertThat(caseSARDetails).hasSize(1)
    assertThat(caseSARDetails[0].caseId).isEqualTo("5678")
    assertThat(caseSARDetails[0].hearings).hasSize(2)
    assertThat(caseSARDetails[0].hearings[0].hearingId).isEqualTo("uuid")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[0].hearings[0].notes).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].notes[0].note).isEqualTo("hearing 1 note")
    assertThat(caseSARDetails[0].hearings[0].notes[0].authorSurname).isEqualTo("author1")
    assertThat(caseSARDetails[0].hearings[1].hearingId).isEqualTo("uuid2")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[0].hearings[1].notes).hasSize(1)
    assertThat(caseSARDetails[0].hearings[1].notes[0].note).isEqualTo("hearing 2 note")
    assertThat(caseSARDetails[0].hearings[1].notes[0].authorSurname).isEqualTo("author2")
  }

  @Test
  fun `given defendant crn on multiple cases and hearings, should return 2 cases with 1 hearingSarResponse each in CaseSarResponse`() {
    whenever(hearingDefendantRepository.findAllByDefendantCrn("X340906")).thenReturn(
      listOf(
        hearingDefendant,
        hearingDefendant2,
      ),
    )
    hearingDefendant.hearingOutcome
    whenever(hearingNotesService.getHearingNotes(hearingDefendant, null, null)).thenReturn(
      listOf(
        HearingNotesSarResponse("hearing 1 note", "author1"),
      ),
    )
    whenever(hearingNotesService.getHearingNotes(hearingDefendant2, null, null)).thenReturn(
      listOf(
        HearingNotesSarResponse("hearing 2 note", "author2"),
      ),
    )
    val caseSARDetails = caseDetailsSarService.getCaseSARDetails("X340906", null, null)

    assertThat(caseSARDetails).hasSize(2)
    assertThat(caseSARDetails[0].caseId).isEqualTo("5678")
    assertThat(caseSARDetails[0].hearings).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].hearingId).isEqualTo("uuid")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[0].hearings[0].outcomes).hasSize(0)
    assertThat(caseSARDetails[0].hearings[0].notes[0].note).isEqualTo("hearing 1 note")
    assertThat(caseSARDetails[0].hearings[0].notes[0].authorSurname).isEqualTo("author1")
    assertThat(caseSARDetails[1].caseId).isEqualTo("CASEID2")
    assertThat(caseSARDetails[1].hearings[0].hearingId).isEqualTo("uuid2")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[1].hearings[0].outcomes).hasSize(0)
    assertThat(caseSARDetails[1].hearings[0].notes[0].note).isEqualTo("hearing 2 note")
    assertThat(caseSARDetails[1].hearings[0].notes[0].authorSurname).isEqualTo("author2")
  }

  @Test
  fun `given there are no hearing notes for a case should return the remaining details`() {
    whenever(hearingDefendantRepository.findAllByDefendantCrn("X340906")).thenReturn(
      listOf(
        hearingDefendant,
        hearingDefendant2,
      ),
    )
    hearingDefendant.hearingOutcome

    val caseSARDetails = caseDetailsSarService.getCaseSARDetails("X340906", null, null)

    assertThat(caseSARDetails).hasSize(2)
    assertThat(caseSARDetails[0].caseId).isEqualTo("5678")
    assertThat(caseSARDetails[0].hearings).hasSize(1)
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[0].hearings[0].hearingId).isEqualTo("uuid")
    assertThat(caseSARDetails[0].hearings[0].outcomes).hasSize(0)
    assertThat(caseSARDetails[0].hearings[0].notes).hasSize(0)
    assertThat(caseSARDetails[1].caseId).isEqualTo("CASEID2")
    assertThat(caseSARDetails[1].hearings[0].hearingId).isEqualTo("uuid2")
    assertThat(caseSARDetails[0].hearings[0].hearingEventType).isEqualTo(HearingEventType.RESULTED.name)
    assertThat(caseSARDetails[1].hearings[0].outcomes).hasSize(0)
  }
}
