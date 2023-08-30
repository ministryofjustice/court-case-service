package uk.gov.justice.probation.courtcaseservice.controller.model

import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState.NEW
import uk.gov.justice.probation.courtcaseservice.controller.model.SortOrder.ASC
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

data class HearingOutcomeSearchRequest(
    val state: HearingOutcomeItemState? = null,
    val outcomeType: List<HearingOutcomeType>? = listOf(),
    val sortBy: HearingOutcomeSortFields? = null,
    val order: SortOrder? = ASC,
    val assignedToUuid: String? = null
)