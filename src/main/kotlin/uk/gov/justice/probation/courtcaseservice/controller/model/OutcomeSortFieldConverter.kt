package uk.gov.justice.probation.courtcaseservice.controller.model

import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class OutcomeSortFieldConverter : Converter<String, HearingOutcomeSortFields> {
  override fun convert(source: String): HearingOutcomeSortFields? = HearingOutcomeSortFields.bySortFieldIgnoreCase(source)
    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field \"$source\"")
}
