package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.JudicialResultRepository
import java.util.UUID

class JudicialResultFactory(
  private val repository: JudicialResultRepository,
  val offence: OffenceEntity,
  val label: String = "Example judicial result label",
  val isConvicted: Boolean = false,
  val resultTypeId: String = UUID.randomUUID().toString(),
  val resultText: String = "Example judicial result text",
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      JudicialResultEntity.builder()
        .offence(offence)
        .label(label)
        .isConvictedResult(isConvicted)
        .judicialResultTypeId(resultTypeId)
        .resultText(resultText)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}