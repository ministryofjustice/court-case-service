package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentStatus
import uk.gov.justice.probation.courtcaseservice.service.EnrichmentResultService

@Hidden
@RestController
class EnrichmentResultController(
  private val enrichmentResultService: EnrichmentResultService,
) {
  @PostMapping("/enrichment-results", consumes = [APPLICATION_JSON_VALUE])
  fun persistEnrichmentResult(@Valid @RequestBody request: EnrichmentResultRequest): ResponseEntity<Void> {
    if (request.status in UNSUPPORTED_STATUSES) {
      throw ResponseStatusException(BAD_REQUEST, "Enrichment status ${request.status} is not supported")
    }
    enrichmentResultService.persistEnrichmentResult(request)
    return ResponseEntity.noContent().build()
  }

  private companion object {
    private val UNSUPPORTED_STATUSES = setOf(EnrichmentStatus.PARTIAL, EnrichmentStatus.FAILED)
  }
}
