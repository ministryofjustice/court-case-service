package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepositoryFacade
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType

@Service
class EnrichmentResultService(
  private val enrichmentRepository: DefendantEnrichmentRepository,
  private val defendantRepository: DefendantRepository,
  private val offenderRepository: OffenderRepository,
  private val offenderRepositoryFacade: OffenderRepositoryFacade,
  private val groupedOffenderMatchRepository: GroupedOffenderMatchRepository,
) {
  @Transactional
  fun persistEnrichmentResult(request: EnrichmentResultRequest) {
    val sourceType = SourceType.valueOf(request.sourceType.name)
    val defendants = enrichmentRepository.findDefendantsForEnrichment(sourceType, request.targetId)
    if (defendants.isEmpty()) {
      throw EntityNotFoundException("No defendant found for %s target %s", request.sourceType, request.targetId)
    }
    if (defendants.any { it.isManualUpdate }) {
      throw ResponseStatusException(CONFLICT, "Enrichment target has been manually updated")
    }
    defendants.forEach { defendant ->
      updateDefendant(defendant, request)
      updateGroupedMatches(defendant, request, sourceType)
      defendantRepository.save(defendant)
    }

    val matches = request.groupedOffenderMatches?.matches.orEmpty()
    log.info(
      "Persisted enrichment result: sourceType={} targetId={} status={} enrichedBy={} jobRunId={} " +
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

  private fun updateDefendant(defendant: DefendantEntity, request: EnrichmentResultRequest) {
    var update = defendant
    request.defendant?.let { details ->
      val name = details.name?.let {
        NamePropertiesEntity.builder()
          .title(it.title ?: defendant.name?.title)
          .forename1(it.forename1 ?: defendant.name?.forename1)
          .forename2(it.forename2 ?: defendant.name?.forename2)
          .forename3(defendant.name?.forename3)
          .surname(it.surname ?: defendant.name?.surname)
          .build()
      } ?: defendant.name
      val address = details.address?.let {
        AddressPropertiesEntity.builder()
          .line1(it.line1 ?: defendant.address?.line1)
          .line2(it.line2 ?: defendant.address?.line2)
          .line3(it.line3 ?: defendant.address?.line3)
          .line4(it.line4 ?: defendant.address?.line4)
          .line5(it.line5 ?: defendant.address?.line5)
          .postcode(it.postcode ?: defendant.address?.postcode)
          .build()
      } ?: defendant.address
      update = update
        .withCprUUID(details.cprUUID ?: defendant.cprUUID)
        .withName(name)
        .withDefendantName(name?.fullName ?: defendant.defendantName)
        .withAddress(address)
        .withDateOfBirth(details.dateOfBirth ?: defendant.dateOfBirth)
        .withSex(details.sex?.let(Sex::fromString) ?: defendant.sex)
    }
    if (request.offender != null || request.probation != null) {
      update = update.withOffender(updateOffender(defendant, request))
    }
    defendant.update(update)
  }

  private fun updateOffender(defendant: DefendantEntity, request: EnrichmentResultRequest): OffenderEntity {
    val crn = request.offender?.crn ?: defendant.crn
      ?: throw ResponseStatusException(BAD_REQUEST, "A CRN is required for offender or probation data")
    val existing = offenderRepository.findByCrn(crn).orElseGet { OffenderEntity.builder().crn(crn).build() }
    val probation = request.probation
    val update = existing
      .withPnc(request.offender?.pnc ?: existing.pnc)
      .withCro(request.offender?.cro ?: existing.cro)
      .withProbationStatus(probation?.probationStatus?.let(OffenderProbationStatus::of) ?: existing.probationStatus)
      .withAwaitingPsr(probation?.awaitingPsr ?: existing.awaitingPsr)
      .withBreach(probation?.breach ?: existing.isBreach)
      .withPreSentenceActivity(probation?.preSentenceActivity ?: existing.isPreSentenceActivity)
      .withPreviouslyKnownTerminationDate(probation?.previouslyKnownTerminationDate ?: existing.previouslyKnownTerminationDate)
    return offenderRepositoryFacade.upsertOffender(update)
  }

  private fun updateGroupedMatches(defendant: DefendantEntity, request: EnrichmentResultRequest, sourceType: SourceType) {
    val matches = request.groupedOffenderMatches ?: return
    val matchRequest = GroupedOffenderMatchesRequest(
      matches.matches.map { match ->
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
    defendant.hearingDefendants
      .filter { !it.isDeleted && !it.hearing.isDeleted && !it.hearing.courtCase.isDeleted && it.hearing.sourceType == sourceType }
      .map { it.hearing.caseId }
      .distinct()
      .forEach { caseId ->
        val group = groupedOffenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendant.defendantId)
          .map { OffenderMatchMapper.update(caseId, defendant.defendantId, it, matchRequest) }
          .orElseGet { OffenderMatchMapper.newGroupedMatchesOf(defendant.defendantId, matchRequest, caseId) }
        groupedOffenderMatchRepository.save(group)
      }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(EnrichmentResultService::class.java)
  }
}
