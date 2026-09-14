package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

interface ISarFormatter {
  fun getCreatedBy(name: String?): String = name?.stripNameParts() ?: ""

  fun getLastUpdatedBy(name: String?): String = name?.stripNameParts() ?: ""

  fun getSurname(name: String): String = name.stripNameParts()

  fun getAssignedTo(assignedTo: String?): String = assignedTo?.stripNameParts() ?: ""

  private fun String.stripNameParts(): String {
    val withoutSuffix = split("(").firstOrNull()?.trim() ?: ""
    return withoutSuffix.split(Regex("\\s+")).lastOrNull { it.isNotBlank() } ?: ""
  }
}
