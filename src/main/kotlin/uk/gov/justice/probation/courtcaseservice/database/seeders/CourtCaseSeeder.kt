package uk.gov.justice.probation.courtcaseservice.database.seeders

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.database.factories.CaseCommentsFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.CourtCaseFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingDayFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingDefendantFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingNoteFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.HearingOutcomeFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.JudicialResultFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.OffenceFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.PleaFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.VerdictFactory
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.DefendantFactory
import uk.gov.justice.probation.courtcaseservice.database.seeders.framework.Seeder
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDayRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.JudicialResultRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PleaRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.VerdictRepository
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class CourtCaseSeeder(
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
  entityManager: EntityManager,
) : Seeder(entityManager) {
  private var count: Int = 1
  private var start: LocalDate = LocalDate.now()
  private var days: Int = 1
  private var court: String = "B10JQ"
  private var shouldCleanDatabase: Boolean = false
  private val hearingDates = getHearingDates(start, days)

  override fun shouldClean(): Boolean = shouldCleanDatabase

  override fun seed() {
    CourtCaseFactory(courtCaseRepository)
      .withCaseMarkers("High profile", "Sensitive")
      .count(count)
      .forEach { case ->
        CaseCommentsFactory(caseCommentsRepository, caseId = case.caseId).count(1)
        val defendant = DefendantFactory(defendantRepository).count(1).first()
        case.addCaseDefendant(defendant)
        val hearing = HearingFactory(hearingRepository, case).count(1).first()
        hearingDates.forEach { hearingDate ->
          HearingDayFactory(
            headingDayRepository,
            hearing = hearing,
            courtCode = court,
            date = hearingDate,
          ).count(1).forEach { day ->
            hearing.hearingDays.add(day)
          }
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
    court: String,
    shouldClean: Boolean = false,
  ): CourtCaseSeeder {
    this.count = count
    this.start = start
    this.days = days
    this.court = court
    this.shouldCleanDatabase = shouldClean
    return this
  }

  /**
   * Returns list of working-day dates around `start`:
   * - the previous `days` working days (oldest -> newest),
   * - then `start` if it is a working day,
   * - then the next `days` working days (newest at end).
   *
   * Working days are Mon–Fri.
   */
  fun getHearingDates(start: LocalDate, days: Int): List<LocalDate> {
    val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    fun LocalDate.isWorkingDay() = dayOfWeek !in weekend
    val center = if (start.isWorkingDay()) listOf(start) else emptyList()

    val previous = buildList {
      var d = start
      while (size < days) {
        d = d.minusDays(1)
        if (d.isWorkingDay()) add(d)
      }
    }.asReversed() // oldest -> newest

    val next = buildList {
      var d = start
      while (size < days) {
        d = d.plusDays(1)
        if (d.isWorkingDay()) add(d)
      }
    }

    return previous + center + next
  }
}
