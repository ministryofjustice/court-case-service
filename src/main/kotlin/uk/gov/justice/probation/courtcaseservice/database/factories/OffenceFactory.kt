package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.data.Faker
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceRepository

class OffenceFactory(
  private val repository: OffenceRepository,
  val plea: PleaEntity? = null,
  val verdict: VerdictEntity? = null,
  val hearingDefendant: HearingDefendantEntity? = null,
  val title: String = "",
  val summary: String = "",
  val act: String = "",
  val offenceCode: String = "",
  val sequence: Int = 1,
) {
  private val faker = Faker()

  fun count(count: Int = 1): List<OffenceEntity> = Factory(
    newModel = {
      val archetype = faker.offence().archetype()
      OffenceEntity.builder()
        .id(null)
        .plea(plea)
        .verdict(verdict)
        .hearingDefendant(hearingDefendant)
        .title(title.ifEmpty { archetype.title })
        .summary(summary.ifEmpty { archetype.summary })
        .act(act.ifEmpty { archetype.act })
        .offenceCode(offenceCode.ifEmpty { archetype.offenceCode })
        .sequence(sequence)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
