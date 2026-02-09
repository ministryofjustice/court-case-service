package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository

class HearingDefendantFactory(
  private val repository: HearingDefendantRepository,
  val hearing: HearingEntity,
  val defendant: DefendantEntity,
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      HearingDefendantEntity.builder()
        .hearing(hearing)
        .defendant(defendant)
        .defendantId(defendant.defendantId)
        .notes(mutableListOf())
        .offences(mutableListOf())
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}