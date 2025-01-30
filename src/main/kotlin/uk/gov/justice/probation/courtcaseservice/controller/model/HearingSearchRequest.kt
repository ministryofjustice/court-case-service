package uk.gov.justice.probation.courtcaseservice.controller.model

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
    val hearingStatus: HearingStatus? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate = LocalDate.now(),
    val page: Int = 1,
    val size: Int = 20,
    var numberOfPossibleMatches: Long? = null,
    var forename: String? = null,
    var surname: String? = null,
    var defendantName: String? = null,
    var caseId: String? = null,
    var hearingId: String? = null,
    var defendantId: String? = null,
)