package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType

@Service
class MatchEnrichmentService(
  private val groupedOffenderMatchRepository: GroupedOffenderMatchRepository,
) {
  fun updateGroupedMatches(defendantEntity: DefendantEntity, enrichmentResultRequest: EnrichmentResultRequest, sourceType: SourceType) {
    val submittedMatches = enrichmentResultRequest.groupedOffenderMatches?.matches ?: return
    val matches = submittedMatches.distinctBy { it.matchIdentifiers.crn.trim().uppercase() }
    if (matches.size < submittedMatches.size) {
      log.warn(
        "Discarded duplicate enrichment matches: sourceType={} targetId={} jobRunId={} submittedMatchCount={} distinctCrnCount={}",
        enrichmentResultRequest.sourceType,
        enrichmentResultRequest.targetId,
        enrichmentResultRequest.jobRunId,
        submittedMatches.size,
        matches.size,
      )
    }
    val matchRequest = GroupedOffenderMatchesRequest(
      matches.map { match ->
        val identifiers = match.matchIdentifiers
        OffenderMatchRequest(
          MatchIdentifiers(
            identifiers.crn,
            identifiers.pnc,
            identifiers.cro,
            identifiers.aliases?.map { OffenderMatchAlias(null, it.firstName, it.middleNames, it.surname, null) },
          ),
          MatchType.valueOf(match.matchType.name),
          match.confirmed,
          match.rejected,
          null,
        )
      },
    )
    defendantEntity.hearingDefendants
      .filter { !it.isDeleted && !it.hearing.isDeleted && !it.hearing.courtCase.isDeleted && it.hearing.sourceType == sourceType }
      .map { it.hearing.caseId }
      .distinct()
      .forEach { caseId ->
        val group = groupedOffenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantEntity.defendantId)
          .map { OffenderMatchMapper.update(caseId, defendantEntity.defendantId, it, matchRequest) }
          .orElseGet { OffenderMatchMapper.newGroupedMatchesOf(defendantEntity.defendantId, matchRequest, caseId) }
        groupedOffenderMatchRepository.save(group)
      }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(EnrichmentResultService::class.java)
  }
}
