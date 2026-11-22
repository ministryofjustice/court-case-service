package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class DefendantEnrichmentService(
  private val defendantEnrichmentRepository: DefendantEnrichmentRepository,
) {
  fun getCommonPlatformEnrichmentCandidates(
    fromDate: LocalDate,
    toDate: LocalDate,
    courtCode: String,
  ): CommonPlatformEnrichmentTargets {
    val candidates = defendantEnrichmentRepository.findCommonPlatformEnrichmentCandidates(
      fromDate,
      toDate,
      courtCode,
    )
    return CommonPlatformEnrichmentTargets(candidates)
  }

  fun getLibraEnrichmentCandidates(
    fromDate: LocalDate,
    toDate: LocalDate,
    courtCode: String,
  ): LibraEnrichmentTargets {
    val candidates = defendantEnrichmentRepository.findLibraEnrichmentCandidates(
      fromDate,
      toDate,
      courtCode,
    )
    return LibraEnrichmentTargets(candidates)
  }
}
