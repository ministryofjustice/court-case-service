package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions
import org.junit.Assert
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

@ExtendWith(MockitoExtension::class)
internal class HearingOutcomeTypeConverterTest {

    @InjectMocks
    lateinit var hearingOutcomeTypeConverter: HearingOutcomeTypeConverter

    @Test
    fun `should convert string to hearing outcome type enum`() {
        Assertions.assertThat(hearingOutcomeTypeConverter.convert("ADJOURNED")).isEqualTo(HearingOutcomeType.ADJOURNED)
    }

    @Test
    fun `should throw error on invalid outcome type`() {
        Assert.assertThrows(
            "Invalid value INVALID for hearing outcome type",
            HttpClientErrorException::class.java
        ) {
            hearingOutcomeTypeConverter.convert("INVALID")
        }
    }
}
