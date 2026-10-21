package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest

@Service
class EnrichmentResultService {
  fun persistEnrichmentResult(request: EnrichmentResultRequest) {
    val matches = request.groupedOffenderMatches?.matches.orEmpty()
    log.info(
      "Received enrichment result: sourceType={} targetId={} status={} enrichedBy={} jobRunId={} " +
        "hasDefendant={} hasOffender={} hasProbation={} matchCount={} confirmedMatchCount={} rejectedMatchCount={} " +
        "warningCount={} errorCount={}",
      request.sourceType,
      request.targetId,
      request.status,
      request.enrichedBy,
      request.jobRunId,
      request.defendant != null,
      request.offender != null,
      request.probation != null,
      matches.size,
      matches.count { it.confirmed },
      matches.count { it.rejected },
      request.warnings.size,
      request.errors.size,
    )
  }

  private companion object {
    private val log = LoggerFactory.getLogger(EnrichmentResultService::class.java)
  }
}
