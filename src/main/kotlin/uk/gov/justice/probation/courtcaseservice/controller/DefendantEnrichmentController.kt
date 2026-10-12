package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    @RequestParam(required = false) courtCode: String?,
    @RequestParam @Min(0) page: Int,
    @RequestParam @Min(1) @Max(500) size: Int,
  ): CommonPlatformEnrichmentTargets {
    validateDateRange(fromDate, toDate)
    return defendantEnrichmentService.getCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode, page, size)
  }

  @GetMapping("/cids", produces = [APPLICATION_JSON_VALUE])
  fun getLibraEnrichmentCandidates(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate,
    @RequestParam(required = false) courtCode: String?,
    @RequestParam @Min(0) page: Int,
    @RequestParam @Min(1) @Max(500) size: Int,
  ): LibraEnrichmentTargets {
    validateDateRange(fromDate, toDate)
    return defendantEnrichmentService.getLibraEnrichmentCandidates(fromDate, toDate, courtCode, page, size)
  }

  private fun validateDateRange(fromDate: LocalDate, toDate: LocalDate) {
    if (!toDate.isAfter(fromDate)) {
      throw ResponseStatusException(BAD_REQUEST, "toDate must be after fromDate")
    }
  }
}
