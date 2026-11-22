package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.service.DefendantEnrichmentService
import java.time.LocalDate

@Hidden
@RestController
@RequestMapping("/enrichment-targets")
class DefendantEnrichmentController(
  private val defendantEnrichmentService: DefendantEnrichmentService,
) {
  @GetMapping("/defendant-ids", produces = [APPLICATION_JSON_VALUE])
  fun getCommonPlatformEnrichmentCandidates(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
    @RequestParam courtCode: String,
  ): CommonPlatformEnrichmentTargets {
    validateRequest(fromDate, toDate, courtCode)
    return defendantEnrichmentService.getCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode)
  }

  @GetMapping("/cids", produces = [APPLICATION_JSON_VALUE])
  fun getLibraEnrichmentCandidates(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
    @RequestParam courtCode: String,
  ): LibraEnrichmentTargets {
    validateRequest(fromDate, toDate, courtCode)
    return defendantEnrichmentService.getLibraEnrichmentCandidates(fromDate, toDate, courtCode)
  }

  private fun validateRequest(fromDate: LocalDate, toDate: LocalDate, courtCode: String) {
    if (!toDate.isAfter(fromDate)) {
      throw ResponseStatusException(BAD_REQUEST, "toDate must be after fromDate")
    }
    if (courtCode.isBlank() || ',' in courtCode) {
      throw ResponseStatusException(BAD_REQUEST, "A single courtCode is required")
    }
  }
}
