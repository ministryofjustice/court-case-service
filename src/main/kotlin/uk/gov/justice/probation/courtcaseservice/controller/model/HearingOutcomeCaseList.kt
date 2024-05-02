package uk.gov.justice.probation.courtcaseservice.controller.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeAssignedUser

@Schema(description = "Hearing outcome response model")
data class HearingOutcomeCaseList(
    val cases: List<HearingOutcomeResponse>,
    val countsByState: HearingOutcomeCountByState,
    val courtRoomFilters: List<String> = listOf(),
    val totalPages: Int = 0,
    val page: Int = 0,
    val totalElements: Int = 0,
    val assignedUsers: List<HearingOutcomeAssignedUser>
)