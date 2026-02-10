package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType
import java.time.LocalDateTime

class HearingOutcomeFactory(
  private val repository: HearingOutcomeRepository,
  val hearingDefendant: HearingDefendantEntity,
  val assignTo: String = "user_id",
  val outcomeType: HearingOutcomeType = HearingOutcomeType.ADJOURNED,
  val outcomeState: HearingOutcomeItemState = HearingOutcomeItemState.IN_PROGRESS,
  val outcomeDate: LocalDateTime = LocalDateTime.now(),
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      HearingOutcomeEntity.builder()
        .hearingDefendant(hearingDefendant)
        .outcomeType(outcomeType.name)
        .assignedToUuid(assignTo)
        .outcomeDate(outcomeDate)
        .state(outcomeState.name)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
