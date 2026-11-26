package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.service.mapper.EnrichedDefendantMapper

@Service
class EnrichmentResultService(
  private val enrichmentRepository: DefendantEnrichmentRepository,
  private val defendantRepository: DefendantRepository,
  private val offenderEnrichmentService: OffenderEnrichmentService,
  private val matchEnrichmentService: MatchEnrichmentService,
) {
  @Transactional
  fun persistEnrichmentResult(enrichmentResultRequest: EnrichmentResultRequest) {
    val sourceType = SourceType.valueOf(enrichmentResultRequest.sourceType.name)
    val defendants = when (sourceType) {
      SourceType.COMMON_PLATFORM -> listOfNotNull(enrichmentRepository.findCommonPlatformDefendantForEnrichment(enrichmentResultRequest.targetId))
      SourceType.LIBRA -> enrichmentRepository.findLibraDefendantsForEnrichment(enrichmentResultRequest.targetId)
    }
    if (defendants.isEmpty()) {
      logRejection(enrichmentResultRequest, "TARGET_NOT_FOUND")
      throw EntityNotFoundException("No defendant found for %s target %s", enrichmentResultRequest.sourceType, enrichmentResultRequest.targetId)
    }
    if (defendants.any { it.isManualUpdate }) {
      log.info(
        "Rejected enrichment result: reason=MANUALLY_UPDATED sourceType={} targetId={} jobRunId={}",
        enrichmentResultRequest.sourceType,
        enrichmentResultRequest.targetId,
        enrichmentResultRequest.jobRunId,
      )
      throw ResponseStatusException(CONFLICT, "Enrichment target has been manually updated")
    }
    defendants.forEach { defendant ->
      updateDefendant(defendant, enrichmentResultRequest)
      matchEnrichmentService.updateGroupedMatches(defendant, enrichmentResultRequest, sourceType)
      defendantRepository.save(defendant)
    }

    TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
      override fun afterCommit() {
        logPersistedResult(enrichmentResultRequest, defendants.size)
      }
    })
  }

  private fun logPersistedResult(request: EnrichmentResultRequest, updatedDefendantCount: Int) {
    val matches = request.groupedOffenderMatches?.matches.orEmpty()
    log.info(
      "Persisted enrichment result: sourceType={} targetId={} status={} enrichedBy={} jobRunId={} " +
        "updatedDefendantCount={} hasDefendant={} hasOffender={} hasProbation={} " +
        "submittedMatchCount={} submittedConfirmedMatchCount={} submittedRejectedMatchCount={} " +
        "submittedWarningCount={} submittedErrorCount={}",
      request.sourceType,
      request.targetId,
      request.status,
      request.enrichedBy,
      request.jobRunId,
      updatedDefendantCount,
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

  private fun updateDefendant(defendantEntity: DefendantEntity, enrichmentResultRequest: EnrichmentResultRequest) {
    var updatedDefendant = enrichmentResultRequest.defendant?.let { EnrichedDefendantMapper.mergeDefendantDetails(defendantEntity, it) } ?: defendantEntity
    if (enrichmentResultRequest.offender != null || enrichmentResultRequest.probation != null) {
      updatedDefendant = updatedDefendant.withOffender(offenderEnrichmentService.updateOffender(defendantEntity, enrichmentResultRequest))
    }
    defendantEntity.update(updatedDefendant)
  }

  private fun logRejection(request: EnrichmentResultRequest, reason: String) {
    log.warn(
      "Rejected enrichment result: reason={} sourceType={} targetId={} jobRunId={}",
      reason,
      request.sourceType,
      request.targetId,
      request.jobRunId,
    )
  }

  private companion object {
    private val log = LoggerFactory.getLogger(EnrichmentResultService::class.java)
  }
}
