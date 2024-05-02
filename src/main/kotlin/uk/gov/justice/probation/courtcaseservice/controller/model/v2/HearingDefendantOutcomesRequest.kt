package uk.gov.justice.probation.courtcaseservice.controller.model.v2

import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSortFields
import uk.gov.justice.probation.courtcaseservice.controller.model.SortOrder
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType


data class HearingDefendantOutcomesRequest(
    val outcomeTypes: List<HearingOutcomeType>? = listOf(),
    val state: HearingOutcomeItemState? = null,
    val assignedUsers: List<String>? = listOf(),
    val courtRooms: List<String> = listOf(),
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: HearingOutcomeSortFields? = null,
    val order: SortOrder? = SortOrder.ASC
)