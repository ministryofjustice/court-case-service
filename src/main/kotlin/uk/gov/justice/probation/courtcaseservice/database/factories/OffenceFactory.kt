package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceRepository

class OffenceFactory(
  private val repository: OffenceRepository,
  val plea: PleaEntity,
  val verdict: VerdictEntity,
  val hearingDefendant: HearingDefendantEntity,
  val title: String = "Example offence title",
  val summary: String = "Example summary of the offence",
  val sequence: Int = 1,
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      OffenceEntity.builder()
        .id(null)
        .plea(plea)
        .verdict(verdict)
        .hearingDefendant(hearingDefendant)
        .title(title)
        .summary(summary)
        .sequence(sequence)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}