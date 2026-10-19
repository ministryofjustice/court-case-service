package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DefendantEnrichmentServiceTest {
  private val fromDate = LocalDate.of(2026, 10, 9)
  private val toDate = LocalDate.of(2026, 10, 23)

  @Mock
  lateinit var repository: DefendantEnrichmentRepository

  @InjectMocks
  lateinit var service: DefendantEnrichmentService

  @Test
  fun `should select common platform candidates independently`() {
    val pageable = PageRequest.of(0, 1)
    given(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B10JQ", pageable))
      .willReturn(PageImpl(listOf("cp-defendant-id"), pageable, 3L))

    assertThat(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, "B10JQ", 0, 1))
      .isEqualTo(CommonPlatformEnrichmentTargets(listOf("cp-defendant-id"), 0, 1, 3L, 3, true))

    verify(repository).findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B10JQ", pageable)
    verifyNoMoreInteractions(repository)
  }

  @Test
  fun `should select libra candidates independently`() {
    val pageable = PageRequest.of(1, 2)
    given(repository.findLibraEnrichmentCandidates(fromDate, toDate, null, pageable))
      .willReturn(PageImpl(listOf("c12345"), pageable, 3L))

    assertThat(service.getLibraEnrichmentCandidates(fromDate, toDate, null, 1, 2))
      .isEqualTo(LibraEnrichmentTargets(listOf("c12345"), 1, 2, 3L, 2, false))

    verify(repository).findLibraEnrichmentCandidates(fromDate, toDate, null, pageable)
    verifyNoMoreInteractions(repository)
  }

  @Test
  fun `should return no common platform candidates when the query is empty`() {
    val pageable = PageRequest.of(0, 200)
    given(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, null, pageable))
      .willReturn(Page.empty(pageable))

    assertThat(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, null, 0, 200))
      .isEqualTo(CommonPlatformEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))
  }

  @Test
  fun `should return no libra candidates when the query is empty`() {
    val pageable = PageRequest.of(0, 200)
    given(repository.findLibraEnrichmentCandidates(fromDate, toDate, null, pageable))
      .willReturn(Page.empty(pageable))

    assertThat(service.getLibraEnrichmentCandidates(fromDate, toDate, null, 0, 200))
      .isEqualTo(LibraEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))
  }

  @Test
  fun `should preserve totals for a page beyond the last page`() {
    val pageable = PageRequest.of(5, 2)
    given(repository.findLibraEnrichmentCandidates(fromDate, toDate, null, pageable))
      .willReturn(PageImpl(emptyList(), pageable, 3L))

    assertThat(service.getLibraEnrichmentCandidates(fromDate, toDate, null, 5, 2))
      .isEqualTo(LibraEnrichmentTargets(emptyList(), 5, 2, 3L, 2, false))
  }
}
