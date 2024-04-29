package uk.gov.justice.probation.courtcaseservice.service.v2

import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.filters.*

import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCaseList as V2HearingOutcomeCaseList

class FilterHearingDefendantOutcomesService(private val hearingDefendantOutcomes: V2HearingOutcomeCaseList) {

    fun getResponse(): FilteredHearingDefendantOutcomesResponse {
        val assignedUsers: List<FilterItem> = hearingDefendantOutcomes.assignedUsers.map { (name, id) -> FilterItem(id, name, true) }
        val courtRooms: List<FilterItem> = hearingDefendantOutcomes.courtRoomFilters.map { court -> FilterItem(court, court, true) }
        val hearingOutcomeStates: List<FilterItem> = getFilterableHearingOutcomeStates()

        var filters: MutableList<FiltersList> = mutableListOf();
        filters.add(FiltersList("assignedUsers", "Assigned Users", true, assignedUsers))
        filters.add(FiltersList("courtRooms", "Court Rooms", true, courtRooms))
        filters.add(FiltersList("states", "Hearing Outcome States", true, hearingOutcomeStates))

        return FilteredHearingDefendantOutcomesResponse(hearingDefendantOutcomes.records, filters)
    }

    private fun getFilterableHearingOutcomeStates(): List<FilterItem>{
        return enumValues<HearingOutcomeItemState>().toList().map {
                state -> HearingOutcomeStatesFilterItem(
                state.name,
                state.name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercaseChar) },
                hearingDefendantOutcomeStateMatches(state),
                true
            )
        }
    }

    private fun hearingDefendantOutcomeStateMatches(state: HearingOutcomeItemState): Int{
        val matches = hearingDefendantOutcomes.countsByState?.counts?.find { it.first == state.name }
        if (matches != null) return matches.second!!.toInt() else return 0;
    }
}