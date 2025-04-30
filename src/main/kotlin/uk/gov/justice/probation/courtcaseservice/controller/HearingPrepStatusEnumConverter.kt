package uk.gov.justice.probation.courtcaseservice.controller

import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus

@Component
class HearingPrepStatusEnumConverter : Converter<String, HearingPrepStatus> {
  override fun convert(source: String): HearingPrepStatus? {
    try {
      return HearingPrepStatus.valueOf(source)
    } catch (e: IllegalArgumentException) {
      throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid value $source for hearing prep status")
    }
  }
}
