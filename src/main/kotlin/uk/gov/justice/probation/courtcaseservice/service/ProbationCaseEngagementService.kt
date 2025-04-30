package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusOffenderDetail
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository

@Service
class ProbationCaseEngagementService(
  val defendantRepository: DefendantRepository,
  val offenderRepository: OffenderRepository,
  val telemetryService: TelemetryService,
) {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun updateMatchingDefendantsWithOffender(deliusOffenderDetail: DeliusOffenderDetail?) {
    val matchingDefendants = defendantRepository.findMatchingDefendants(
      deliusOffenderDetail?.identifiers?.pnc,
      deliusOffenderDetail?.dateOfBirth,
      deliusOffenderDetail?.name?.forename,
      deliusOffenderDetail?.name?.surname,
    )

    if (matchingDefendants.isNotEmpty()) {
      log.debug("Mapping new offender with details {}", deliusOffenderDetail)
      val newOffender = createOffender(deliusOffenderDetail)
      matchingDefendants.forEach { it ->
        it.offender = newOffender
        defendantRepository.save(it)
        telemetryService.trackPiCNewEngagementDefendantLinkedEvent(it)
      }
    } else {
      log.debug("No matching defendants found with {}", deliusOffenderDetail)
    }
  }

  fun createOffender(deliusOffenderDetail: DeliusOffenderDetail?): OffenderEntity? {
    log.debug("Creating new offender with details {}", deliusOffenderDetail)
    val newOffender = OffenderEntity(
      null,
      deliusOffenderDetail?.identifiers?.crn,
      deliusOffenderDetail?.identifiers?.pnc,
      "",
      emptyList(),
      OffenderProbationStatus.NOT_SENTENCED,
      true,
      false,
      false,
      false,
      null,
    )
    return offenderRepository.save(newOffender)
  }
}
