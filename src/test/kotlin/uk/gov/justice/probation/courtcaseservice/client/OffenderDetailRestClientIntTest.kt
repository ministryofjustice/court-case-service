package uk.gov.justice.probation.courtcaseservice.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException
import java.time.LocalDate

class OffenderDetailRestClientIntTest : BaseIntTest() {

    @Autowired
    lateinit var restClient: OffenderDetailRestClient

    @Test
    fun `should return delius offender detail for valid CRN`() {
        // Given
        val crn = "C1234"

        // When
        val deliusOffenderDetail = restClient.getOffenderDetail("/probation-case.engagement.created/$crn", crn).block()

        // Then
        assertThat(deliusOffenderDetail?.identifiers?.crn).isEqualTo(crn)
        assertThat(deliusOffenderDetail?.dateOfBirth).isEqualTo(LocalDate.of(1939, 10, 10))
    }

    @Test
    fun `should throw forbidden exception when not authorised`() {
        // Given
        val crn = "AB12345"

        // When
        val exception = assertThrows(ForbiddenException::class.java) {
            restClient.getOffenderDetail("/probation-case.engagement.created/$crn", crn).block()
        }

        // Then
        assertThat(exception.message).isEqualTo("You are excluded from viewing this offender record. Please contact a system administrator")
    }
}