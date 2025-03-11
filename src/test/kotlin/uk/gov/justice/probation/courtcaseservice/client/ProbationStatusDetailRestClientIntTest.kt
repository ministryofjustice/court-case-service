package uk.gov.justice.probation.courtcaseservice.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.model.ProbationStatus
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException
import java.time.LocalDate

class ProbationStatusDetailRestClientIntTest : BaseIntTest() {

  @Autowired
  lateinit var restClient: ProbationStatusDetailRestClient

  @Test
  fun `should return probation status for valid CRN`() {
    // Given
    val crn = "2234"

    // When
    val probationStatusDetail = restClient.getProbationStatusByCrn(crn).block()

    // Then
    assertThat(probationStatusDetail?.status).isEqualTo(ProbationStatus.CURRENT.toString())
    assertThat(probationStatusDetail?.previouslyKnownTerminationDate).isEqualTo(LocalDate.of(2010, 4, 5))
    assertThat(probationStatusDetail?.awaitingPsr).isTrue()
    assertThat(probationStatusDetail?.inBreach).isTrue()
    assertThat(probationStatusDetail?.isPreSentenceActivity).isTrue()
  }

  @Test
  fun `should return empty NO RECORD Probation Status Details for unknown CRN`() {
    // Given
    val crn = "CRNXXX"

    // When
    val probationStatusDetail = restClient.getProbationStatusByCrn(crn).block()

    // Then
    assertThat(probationStatusDetail?.status).isEqualTo(ProbationStatus.NO_RECORD.toString())
    assertThat(probationStatusDetail?.previouslyKnownTerminationDate).isNull()
    assertThat(probationStatusDetail?.awaitingPsr).isFalse()
    assertThat(probationStatusDetail?.inBreach).isFalse()
    assertThat(probationStatusDetail?.isPreSentenceActivity).isFalse()
  }

  @Test
  fun `should throw forbidden excpetion when not authorised`() {
    // Given
    val crn = "CRN403"

    // When
    val exception = assertThrows(ForbiddenException::class.java) {
      restClient.getProbationStatusByCrn(crn).block()
    }

    // Then
    assertThat(exception.message).isEqualTo("You are excluded from viewing this offender record. Please contact a system administrator")
  }
}
