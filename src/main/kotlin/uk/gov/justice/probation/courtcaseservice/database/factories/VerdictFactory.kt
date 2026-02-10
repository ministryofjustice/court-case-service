package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.VerdictRepository
import java.time.LocalDate

class VerdictFactory(
  private val repository: VerdictRepository,
  val date: LocalDate = LocalDate.now(),
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      VerdictEntity.builder()
        .typeDescription("verdictTypeDescription1")
        .date(date)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
