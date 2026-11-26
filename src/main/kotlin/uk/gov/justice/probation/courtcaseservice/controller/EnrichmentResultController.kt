package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.TransactionException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.service.EnrichmentResultService

@Hidden
@RestController
class EnrichmentResultController(
  private val enrichmentResultService: EnrichmentResultService,
) {
  @PostMapping("/enrichment-results", consumes = [APPLICATION_JSON_VALUE])
  fun persistEnrichmentResult(@Valid @RequestBody request: EnrichmentResultRequest): ResponseEntity<Void> {
    try {
      enrichmentResultService.persistEnrichmentResult(request)
    } catch (exception: DataAccessException) {
      logPersistenceFailure(request, exception)
      throw exception
    } catch (exception: TransactionException) {
      logPersistenceFailure(request, exception)
      throw exception
    }
    return ResponseEntity.noContent().build()
  }

  private fun logPersistenceFailure(request: EnrichmentResultRequest, exception: RuntimeException) {
    log.error(
      "Failed to persist enrichment result: sourceType={} targetId={} jobRunId={}",
      request.sourceType,
      request.targetId,
      request.jobRunId,
      exception,
    )
  }

  private companion object {
    private val log = LoggerFactory.getLogger(EnrichmentResultController::class.java)
  }
}
