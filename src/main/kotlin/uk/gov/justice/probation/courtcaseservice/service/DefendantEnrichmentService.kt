package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.data.domain.PageRequest
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
    courtCode: String?,
    page: Int,
    size: Int,
  ): CommonPlatformEnrichmentTargets {
    val candidates = defendantEnrichmentRepository.findCommonPlatformEnrichmentCandidates(
      fromDate,
      toDate,
      courtCode,
      PageRequest.of(page, size),
    )
    return CommonPlatformEnrichmentTargets(
      candidates.content,
      candidates.number,
      candidates.size,
      candidates.totalElements,
      candidates.totalPages,
      candidates.hasNext(),
    )
  }

  fun getLibraEnrichmentCandidates(
    fromDate: LocalDate,
    toDate: LocalDate,
    courtCode: String?,
    page: Int,
    size: Int,
  ): LibraEnrichmentTargets {
    val candidates = defendantEnrichmentRepository.findLibraEnrichmentCandidates(
      fromDate,
      toDate,
      courtCode,
      PageRequest.of(page, size),
    )
    return LibraEnrichmentTargets(
      candidates.content,
      candidates.number,
      candidates.size,
      candidates.totalElements,
      candidates.totalPages,
      candidates.hasNext(),
    )
  }
}
