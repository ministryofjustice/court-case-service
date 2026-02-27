package uk.gov.justice.probation.courtcaseservice.database.seeders

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
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
import java.time.LocalDate

class CourtCaseSeederTest {
  private fun seeder(): CourtCaseSeeder = CourtCaseSeeder(
    courtCaseRepository = mock(CourtCaseRepository::class.java),
    hearingRepository = mock(HearingRepository::class.java),
    caseCommentsRepository = mock(CaseCommentsRepository::class.java),
    defendantRepository = mock(DefendantRepository::class.java),
    headingDayRepository = mock(HearingDayRepository::class.java),
    hearingDefendantRepository = mock(HearingDefendantRepository::class.java),
    hearingNoteRepository = mock(HearingNoteRepository::class.java),
    hearingOutcomeRepository = mock(HearingOutcomeRepository::class.java),
    pleaRepository = mock(PleaRepository::class.java),
    verdictRepository = mock(VerdictRepository::class.java),
    offenceRepository = mock(OffenceRepository::class.java),
    judicialResultRepository = mock(JudicialResultRepository::class.java),
    entityManager = mock(EntityManager::class.java),
  )

  @Test
  fun `getHearingDates returns previous start and next working days`() {
    // Start on a Wednesday
    val start = LocalDate.of(2024, 1, 10)

    val dates = seeder().getHearingDates(start, days = 2)

    assertThat(dates).containsExactly(
      LocalDate.of(2024, 1, 8),
      LocalDate.of(2024, 1, 9),
      LocalDate.of(2024, 1, 10),
      LocalDate.of(2024, 1, 11),
      LocalDate.of(2024, 1, 12),
    )
  }

  @Test
  fun `getHearingDates skips weekend start and excludes weekends`() {
    val saturday = LocalDate.of(2024, 1, 13)

    val dates = seeder().getHearingDates(saturday, days = 2)

    assertThat(dates).containsExactly(
      LocalDate.of(2024, 1, 11),
      LocalDate.of(2024, 1, 12),
      LocalDate.of(2024, 1, 15),
      LocalDate.of(2024, 1, 16),
    )
  }
}
