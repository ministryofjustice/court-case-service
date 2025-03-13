package uk.gov.justice.probation.courtcaseservice.controller.model

import uk.gov.justice.probation.courtcaseservice.controller.model.SortOrder.ASC
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

data class HearingOutcomeSearchRequest(
  val state: HearingOutcomeItemState? = null,
  val outcomeType: List<HearingOutcomeType>? = listOf(),
  val sortBy: HearingOutcomeSortFields? = null,
  val order: SortOrder? = ASC,
  val courtRoom: List<String> = listOf(),
  val assignedToUuid: List<String>? = listOf(),
  val page: Int = 1,
  val size: Int = 20,
)
