package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository

@Service
class OffenderEnrichmentService(
  private val offenderRepository: OffenderRepository,
) {
  fun updateOffender(defendant: DefendantEntity, enrichmentResultRequest: EnrichmentResultRequest): OffenderEntity {
    val crn = enrichmentResultRequest.offender?.crn ?: defendant.crn ?: rejectMissingCrn(enrichmentResultRequest)
    val existingOffender = offenderRepository.findByCrn(crn).orElse(null)
    val offenderToUpdate = existingOffender ?: OffenderEntity.builder().crn(crn).build()
    val probation = enrichmentResultRequest.probation
    val probationStatus = probation?.probationStatus?.let(OffenderProbationStatus::of) ?: offenderToUpdate.probationStatus
      ?: rejectMissingProbationStatus(enrichmentResultRequest)
    val updatedOffender = offenderToUpdate
      .withPnc(enrichmentResultRequest.offender?.pnc ?: offenderToUpdate.pnc)
      .withCro(enrichmentResultRequest.offender?.cro ?: offenderToUpdate.cro)
      .withProbationStatus(probationStatus)
      .withAwaitingPsr(probation?.awaitingPsr ?: offenderToUpdate.awaitingPsr)
      .withBreach(probation?.breach ?: offenderToUpdate.isBreach)
      .withPreSentenceActivity(probation?.preSentenceActivity ?: offenderToUpdate.isPreSentenceActivity)
      .withPreviouslyKnownTerminationDate(probation?.previouslyKnownTerminationDate ?: offenderToUpdate.previouslyKnownTerminationDate)
    if (existingOffender == null) return offenderRepository.save(updatedOffender)
    if (offenderToUpdate != updatedOffender) existingOffender.update(updatedOffender)
    return existingOffender
  }

  private fun rejectMissingCrn(request: EnrichmentResultRequest): Nothing {
    logRejection(request, "MISSING_CRN")
    throw ResponseStatusException(BAD_REQUEST, "A CRN is required for offender or probation data")
  }

  private fun rejectMissingProbationStatus(request: EnrichmentResultRequest): Nothing {
    logRejection(request, "MISSING_PROBATION_STATUS")
    throw ResponseStatusException(BAD_REQUEST, "A probation status is required when the offender has no existing status")
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
