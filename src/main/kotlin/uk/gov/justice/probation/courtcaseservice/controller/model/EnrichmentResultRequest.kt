package uk.gov.justice.probation.courtcaseservice.controller.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

enum class EnrichmentSourceType {
  COMMON_PLATFORM,
  LIBRA,
}

enum class EnrichmentStatus {
  ENRICHED,
  NO_MATCH,
  PARTIAL,
  FAILED,
  NEEDS_REVIEW,
}

enum class EnrichmentMatchType {
  NAME_DOB_ALIAS,
}

data class EnrichmentResultRequest(
  val sourceType: EnrichmentSourceType,
  @field:NotBlank
  val targetId: String,
  val status: EnrichmentStatus,
  @field:Valid
  val defendant: EnrichedDefendantDetails? = null,
  val offender: EnrichedOffenderDetails? = null,
  val probation: EnrichedProbationDetails? = null,
  @field:Valid
  val groupedOffenderMatches: EnrichedGroupedOffenderMatches? = null,
  val warnings: List<String> = emptyList(),
  val errors: List<String> = emptyList(),
  @field:NotBlank
  val enrichedBy: String,
  @field:Pattern(regexp = ".*\\S.*", message = "must not be blank when supplied")
  val jobRunId: String? = null,
)

data class EnrichedOffenderDetails(
  val crn: String? = null,
  val pnc: String? = null,
  val cro: String? = null,
)

data class EnrichedProbationDetails(
  val probationStatus: String? = null,
  val awaitingPsr: Boolean? = null,
  val breach: Boolean? = null,
  val previouslyKnownTerminationDate: LocalDate? = null,
  val preSentenceActivity: Boolean? = null,
)

data class EnrichedDefendantDetails(
  val cprUUID: String? = null,
  val name: EnrichedName? = null,
  val dateOfBirth: LocalDate? = null,
  val sex: String? = null,
  val address: EnrichedAddress? = null,
)

data class EnrichedName(
  val title: String? = null,
  val forename1: String? = null,
  val forename2: String? = null,
  val surname: String? = null,
)

data class EnrichedAddress(
  val line1: String? = null,
  val line2: String? = null,
  val line3: String? = null,
  val line4: String? = null,
  val line5: String? = null,
  val postcode: String? = null,
)

data class EnrichedGroupedOffenderMatches(
  @field:Valid
  val matches: List<EnrichedOffenderMatch>,
)

data class EnrichedOffenderMatch(
  @field:Valid
  val matchIdentifiers: EnrichedMatchIdentifiers,
  val matchType: EnrichmentMatchType = EnrichmentMatchType.NAME_DOB_ALIAS,
  val confirmed: Boolean = false,
  val rejected: Boolean = false,
)

data class EnrichedMatchIdentifiers(
  @field:NotBlank
  val crn: String,
  val pnc: String? = null,
  val cro: String? = null,
  val aliases: List<EnrichedAlias>? = null,
)

data class EnrichedAlias(
  val firstName: String? = null,
  val surname: String? = null,
  val middleNames: List<String>? = null,
)
