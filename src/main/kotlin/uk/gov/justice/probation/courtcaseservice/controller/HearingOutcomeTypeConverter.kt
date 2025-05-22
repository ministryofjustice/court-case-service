package uk.gov.justice.probation.courtcaseservice.controller

import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType

@Component
class HearingOutcomeTypeConverter : Converter<String, HearingOutcomeType> {
  override fun convert(source: String): HearingOutcomeType? {
    try {
      return HearingOutcomeType.valueOf(source)
    } catch (e: IllegalArgumentException) {
      throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid value $source for hearing outcome type")
    }
  }
}
