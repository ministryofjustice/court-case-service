package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.SharePointClient
import uk.gov.justice.probation.courtcaseservice.jpa.repository.BailInformationServiceRepository
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@Service
class BailInformationService(
  private val repository: BailInformationServiceRepository,
  private val sharePointClient: SharePointClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(BailInformationService::class.java)
  }

  fun exportReport() {
    log.info("Fetching bail information report data")
    val rows = repository.fetchBailInformationReport()
    log.info("Fetched {} rows for bail information report", rows.size)

    val fileName = "bail_information_report_${LocalDate.now()}.csv"
    val csv = buildString {
      appendLine(toCsvLine(BailInformationReportRow.CSV_HEADERS))
      rows.forEach { row ->
        appendLine(toCsvLine(BailInformationReportRow.toCsvRow(row)))
      }
    }
    log.info("Bail information report generated in memory")

    sharePointClient.uploadFile(fileName, csv.toByteArray(StandardCharsets.UTF_8))
  }

  private fun toCsvLine(values: List<String?>): String = values.joinToString(",") { value -> formatCell(value) }

  private fun formatCell(value: String?): String = when (value) {
    null -> ""
    else -> {
      val escaped = value.replace("\"", "\"\"")
      if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n')) "\"$escaped\"" else escaped
    }
  }
}
