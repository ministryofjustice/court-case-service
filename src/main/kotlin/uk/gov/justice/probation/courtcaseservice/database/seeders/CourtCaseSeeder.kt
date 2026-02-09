package uk.gov.justice.probation.courtcaseservice.database.seeders

import java.util.Optional
import java.time.LocalDate
import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.database.seeders.framework.Seeder
import uk.gov.justice.probation.courtcaseservice.database.factories.PleaFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.VerdictFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.OffenceFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.CourtCaseFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingDayFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingNoteFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.CaseCommentsFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingOutcomeFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.JudicialResultFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingDefendantFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.DefendantFactory
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PleaRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.VerdictRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDayRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.JudicialResultRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository

@Component
class CourtCaseSeeder (
  private val courtCaseRepository: CourtCaseRepository,
  private val hearingRepository: HearingRepository,
  private val caseCommentsRepository: CaseCommentsRepository,
  private val defendantRepository: DefendantRepository,
  private val headingDayRepository: HearingDayRepository,
  private val hearingDefendantRepository: HearingDefendantRepository,
  private val hearingNoteRepository: HearingNoteRepository,
  private val hearingOutcomeRepository: HearingOutcomeRepository,
  private val pleaRepository: PleaRepository,
  private val verdictRepository: VerdictRepository,
  private val offenceRepository: OffenceRepository,
  private val judicialResultRepository: JudicialResultRepository,
) : Seeder {
  private var count: Int = 1
  private var start: LocalDate = LocalDate.now()
  private var days: Int = 1
  private var court: Optional<String> = Optional.empty()


  override fun run() {
    CourtCaseFactory(courtCaseRepository)
      .withCaseMarkers("High profile", "Sensitive")
      .count(count)
      .forEach { case ->
        CaseCommentsFactory(caseCommentsRepository, caseId = case.caseId).count(1)
        val defendant = DefendantFactory(defendantRepository).count(1).first()
        case.addCaseDefendant(defendant)
        val hearing = HearingFactory(hearingRepository, case).count(1).first()
        HearingDayFactory(headingDayRepository, hearing = hearing).count(1).forEach { day ->
          hearing.hearingDays.add(day)
        }
        val hearingDefendant = HearingDefendantFactory(hearingDefendantRepository, hearing, defendant).count(1).first()
        val hearingNote = HearingNoteFactory(hearingNoteRepository, hearing).count(1).first()
        hearingDefendant.addHearingNote(hearingNote)
        HearingOutcomeFactory(hearingOutcomeRepository, hearingDefendant).count(1)
        val plea = PleaFactory(pleaRepository).count(1).first()
        val verdict = VerdictFactory(verdictRepository).count(1).first()
        val offence = OffenceFactory(offenceRepository, plea, verdict, hearingDefendant).count(1).first()
        JudicialResultFactory(judicialResultRepository, offence).count(1)
      }
  }
  fun options(
    count: Int = 1,
    start: LocalDate = LocalDate.now(),
    days: Int = 1,
    court: Optional<String>
  ): CourtCaseSeeder {
    this.count = count;
    this.start = start;
    this.days = days;
    this.court = court;
    return this
  }
}
