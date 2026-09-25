package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

interface ISarFormatter {
  fun getCreatedBy(name: String?): String = name?.split("(")?.first() ?: ""

  fun getLastUpdatedBy(name: String?): String = name?.split("(")?.first() ?: ""

  fun getSurname(name: String): String = name.split(" ").last()

  fun getAssignedTo(assignedTo: String?): String = assignedTo?.split(" ")?.last() ?: ""
}
