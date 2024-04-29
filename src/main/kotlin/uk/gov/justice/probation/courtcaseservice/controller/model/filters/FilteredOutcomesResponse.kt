package uk.gov.justice.probation.courtcaseservice.controller.model.filters

import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeResponse

data class FilteredHearingDefendantOutcomesResponse(
    override val records: List<HearingOutcomeResponse>,
    override val filters: MutableList<FiltersList>
): FilteredResourcesResponse(records, filters)