package uk.gov.justice.probation.courtcaseservice.controller.model.filters

open class FilteredResourcesResponse(
    open val records: Any?,
    open val filters: MutableList<FiltersList>
)