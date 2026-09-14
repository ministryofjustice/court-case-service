package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PleaRepository
import java.time.LocalDate

class PleaFactory(
  private val repository: PleaRepository,
  val date: LocalDate = LocalDate.now(),
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      PleaEntity.builder()
        .value("pleaValue1")
        .date(date)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
