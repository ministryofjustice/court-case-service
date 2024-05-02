package uk.gov.justice.probation.courtcaseservice.controller.model.v2

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeAssignedUser

@Schema(description = "V2 Hearing outcome response model")
data class HearingOutcomeCaseList(
    val records: List<HearingOutcomeResponse> = listOf(),
    val countsByState: HearingOutcomeCountByState? = null,
    val courtRoomFilters: List<String> = listOf(),
    val totalPages: Int = 0,
    val page: Int = 0,
    val totalElements: Int = 0,
    val assignedUsers: List<HearingOutcomeAssignedUser> = listOf()
)