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
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DefendantEnrichmentServiceTest {
  private val fromDate = LocalDate.of(2026, 10, 9)
  private val toDate = LocalDate.of(2026, 10, 23)
  private val courtCode = "B10JQ"

  @Mock
  lateinit var repository: DefendantEnrichmentRepository

  @InjectMocks
  lateinit var service: DefendantEnrichmentService

  @Test
  fun `should return all common platform candidates`() {
    val ids = (1..501).map { "id-$it" }
    given(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode)).willReturn(ids)

    assertThat(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode))
      .isEqualTo(CommonPlatformEnrichmentTargets(ids))
    verify(repository).findCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode)
    verifyNoMoreInteractions(repository)
  }

  @Test
  fun `should return all libra candidates`() {
    val ids = (1..501).map { "c$it" }
    given(repository.findLibraEnrichmentCandidates(fromDate, toDate, courtCode)).willReturn(ids)

    assertThat(service.getLibraEnrichmentCandidates(fromDate, toDate, courtCode))
      .isEqualTo(LibraEnrichmentTargets(ids))
    verify(repository).findLibraEnrichmentCandidates(fromDate, toDate, courtCode)
    verifyNoMoreInteractions(repository)
  }

  @Test
  fun `should return empty candidate lists`() {
    given(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode)).willReturn(emptyList())
    given(repository.findLibraEnrichmentCandidates(fromDate, toDate, courtCode)).willReturn(emptyList())

    assertThat(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, courtCode))
      .isEqualTo(CommonPlatformEnrichmentTargets(emptyList()))
    assertThat(service.getLibraEnrichmentCandidates(fromDate, toDate, courtCode))
      .isEqualTo(LibraEnrichmentTargets(emptyList()))
  }
}
