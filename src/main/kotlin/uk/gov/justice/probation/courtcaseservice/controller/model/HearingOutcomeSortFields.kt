package uk.gov.justice.probation.courtcaseservice.controller.model

enum class HearingOutcomeSortFields(val sortField: String) {
  HEARING_DATE("hearingDate"),
  ;

  companion object {
    fun bySortFieldIgnoreCase(input: String): HearingOutcomeSortFields? = entries.firstOrNull { it.sortField.equals(input, true) }
  }
}
