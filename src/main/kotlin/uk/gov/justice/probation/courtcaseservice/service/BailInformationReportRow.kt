package uk.gov.justice.probation.courtcaseservice.service

import java.time.LocalDate
import java.time.LocalTime

data class BailInformationReportRow(
  val hearingId: String?,
  val hearingEventType: String?,
  val hearingType: String?,
  val hearingDay: LocalDate?,
  val hearingTime: LocalTime?,
  val offenceSummary: String?,
  val offenceTitle: String?,
  val offenceSequence: Int?,
  val offenceAct: String?,
  val offenceCode: String?,
  val pleaId: String?,
  val verdictId: String?,
  val defendantName: String?,
  val defendantType: String?,
  val defendantNameJson: String?,
  val defendantAddress: String?,
  val crn: String?,
  val pnc: String?,
  val cro: String?,
  val dateOfBirth: LocalDate?,
  val sex: String?,
  val nationality1: String?,
  val nationality2: String?,
  val urn: String?,
  val courtName: String?,
  val courtCode: String?,
) {
  companion object {
    val CSV_HEADERS = listOf(
      "Hearing ID", "Hearing Event Type", "Hearing Type",
      "Hearing Day", "Hearing Time",
      "Offence Summary", "Offence Title", "Offence Sequence", "Offence Act", "Offence Code",
      "Plea ID", "Verdict ID",
      "Defendant Name", "Defendant Type", "Defendant Name (JSON)", "Defendant Address",
      "CRN", "PNC", "CRO", "Date of Birth", "Sex", "Nationality 1", "Nationality 2",
      "URN", "Court Name", "Court Code",
    )

    fun toCsvRow(row: BailInformationReportRow): List<String?> = listOf(
      row.hearingId, row.hearingEventType, row.hearingType,
      row.hearingDay?.toString(), row.hearingTime?.toString(),
      row.offenceSummary, row.offenceTitle, row.offenceSequence?.toString(), row.offenceAct, row.offenceCode,
      row.pleaId, row.verdictId,
      row.defendantName, row.defendantType, row.defendantNameJson, row.defendantAddress,
      row.crn, row.pnc, row.cro, row.dateOfBirth?.toString(), row.sex, row.nationality1, row.nationality2,
      row.urn, row.courtName, row.courtCode,
    )
  }
}
