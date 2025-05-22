package uk.gov.justice.probation.courtcaseservice.controller.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

internal class OutcomeSortFieldConverterTest {
  private val subject: OutcomeSortFieldConverter = OutcomeSortFieldConverter()

  @Test
  fun `should map string to hearing sort field constant`() {
    Assertions.assertThat(subject.convert("hearingDate") == HearingOutcomeSortFields.HEARING_DATE)
  }

  @Test
  fun `should throw bad request when unable to convert string to enum`() {
    assertThrows(
      ResponseStatusException::class.java,
      { subject.convert("XXXXXXX") },
      "Invalid sort field \"XXXXXXX\"",
    )
  }
}
