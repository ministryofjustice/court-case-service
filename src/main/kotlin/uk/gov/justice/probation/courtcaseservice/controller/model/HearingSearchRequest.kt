package uk.gov.justice.probation.courtcaseservice.controller.model

import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import java.time.LocalDate

data class HearingSearchRequest(
    val probationStatus: List<String> = listOf(),
    val courtRoom: List<String> = listOf(),
    val session: List<CourtSession> = listOf(),
    val source: List<SourceType> = listOf(),
    val breach: Boolean = false,
    val recentlyAdded: Boolean = false,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate = LocalDate.now(),
    val page: Int = 1,
    val size: Int = 20
)