package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.probation.courtcaseservice.application.ApplicationExceptionHandler
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentMatchType
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentSourceType
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentStatus
import uk.gov.justice.probation.courtcaseservice.service.EnrichmentResultService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EnrichmentResultControllerTest {
  @Mock
  lateinit var service: EnrichmentResultService

  lateinit var mockMvc: MockMvc

  @BeforeEach
  fun setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(EnrichmentResultController(service))
      .setControllerAdvice(ApplicationExceptionHandler())
      .build()
  }

  @Test
  fun `should map the full request and return 204`() {
    mockMvc.perform(post("/enrichment-results").contentType(APPLICATION_JSON).content(FULL_REQUEST))
      .andExpect(status().isNoContent)

    val captor = argumentCaptor<EnrichmentResultRequest>()
    verify(service).persistEnrichmentResult(captor.capture())
    val request = captor.firstValue

    assertThat(request.sourceType).isEqualTo(EnrichmentSourceType.COMMON_PLATFORM)
    assertThat(request.targetId).isEqualTo("defendant-id")
    assertThat(request.status).isEqualTo(EnrichmentStatus.ENRICHED)
    assertThat(request.defendant!!.cprUUID).isEqualTo("person-record-id")
    assertThat(request.defendant!!.name!!.forename2).isEqualTo("Middle")
    assertThat(request.defendant!!.dateOfBirth).isEqualTo(LocalDate.of(1990, 1, 2))
    assertThat(request.defendant!!.address!!.postcode).isEqualTo("AB1 2CD")
    assertThat(request.offender!!.crn).isEqualTo("X12345")
    assertThat(request.probation!!.awaitingPsr).isFalse()
    assertThat(request.probation!!.breach).isFalse()
    assertThat(request.probation!!.preSentenceActivity).isTrue()
    assertThat(request.probation!!.previouslyKnownTerminationDate).isEqualTo(LocalDate.of(2025, 3, 4))
    val match = request.groupedOffenderMatches!!.matches.single()
    assertThat(match.matchType).isEqualTo(EnrichmentMatchType.NAME_DOB_ALIAS)
    assertThat(match.confirmed).isFalse()
    assertThat(match.rejected).isFalse()
    assertThat(match.matchIdentifiers.aliases!!.single().middleNames).containsExactly("Middle Names")
    assertThat(request.enrichedBy).isEqualTo("court-case-enrichment-worker")
    assertThat(request.jobRunId).isEqualTo("job-run-uuid")
  }

  @ParameterizedTest
  @ValueSource(strings = ["ENRICHED", "NO_MATCH", "NEEDS_REVIEW"])
  fun `should accept a minimal libra request with defaults applied`(status: String) {
    mockMvc.perform(
      post("/enrichment-results").contentType(APPLICATION_JSON)
        .content("""{"sourceType":"LIBRA","targetId":"c12345","status":"$status","enrichedBy":"court-case-enrichment-worker"}"""),
    ).andExpect(status().isNoContent)

    val captor = argumentCaptor<EnrichmentResultRequest>()
    verify(service).persistEnrichmentResult(captor.capture())
    assertThat(captor.firstValue.sourceType).isEqualTo(EnrichmentSourceType.LIBRA)
    assertThat(captor.firstValue.defendant).isNull()
    assertThat(captor.firstValue.groupedOffenderMatches).isNull()
    assertThat(captor.firstValue.warnings).isEmpty()
    assertThat(captor.firstValue.errors).isEmpty()
    assertThat(captor.firstValue.jobRunId).isNull()
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  fun `should reject invalid requests without calling the service`(body: String) {
    mockMvc.perform(post("/enrichment-results").contentType(APPLICATION_JSON).content(body))
      .andExpect(status().isBadRequest)

    verifyNoInteractions(service)
  }

  companion object {
    private const val VALID_BASE = """"sourceType":"COMMON_PLATFORM","targetId":"defendant-id","enrichedBy":"court-case-enrichment-worker""""

    @JvmStatic
    fun invalidRequests(): List<String> = listOf(
      """{"targetId":"defendant-id","status":"ENRICHED","enrichedBy":"worker"}""",
      """{"sourceType":"UNKNOWN","targetId":"defendant-id","status":"ENRICHED","enrichedBy":"worker"}""",
      """{"sourceType":"LIBRA","status":"ENRICHED","enrichedBy":"worker"}""",
      """{"sourceType":"LIBRA","targetId":" ","status":"ENRICHED","enrichedBy":"worker"}""",
      """{$VALID_BASE}""",
      """{$VALID_BASE,"status":"UNKNOWN"}""",
      """{$VALID_BASE,"status":"PARTIAL"}""",
      """{$VALID_BASE,"status":"FAILED"}""",
      """{"sourceType":"LIBRA","targetId":"c1","status":"ENRICHED","enrichedBy":" "}""",
      """{"sourceType":"LIBRA","targetId":"c1","status":"ENRICHED"}""",
      """{$VALID_BASE,"status":"ENRICHED","jobRunId":" "}""",
      """{$VALID_BASE,"status":"ENRICHED","defendant":{"dateOfBirth":"02/01/1990"}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{"matches":[{}]}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{"matches":[{"matchIdentifiers":{}}]}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{"matches":[{"matchIdentifiers":{"crn":""}}]}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{"matches":[{"matchIdentifiers":{"crn":"X1"},"matchType":"OTHER"}]}}""",
      """{$VALID_BASE,"status":"ENRICHED","groupedOffenderMatches":{"matches":[{"matchIdentifiers":{"crn":"X1","aliases":[{"middleNames":"single"}]}}]}}""",
      "not-json",
    )

    const val FULL_REQUEST = """
      {
        "sourceType": "COMMON_PLATFORM",
        "targetId": "defendant-id",
        "status": "ENRICHED",
        "defendant": {
          "cprUUID": "person-record-id",
          "name": {"title": "Mr", "forename1": "Alex", "forename2": "Middle", "surname": "Example"},
          "dateOfBirth": "1990-01-02",
          "sex": "Male",
          "address": {"line1": "Building", "line2": "12", "line3": "Example Street", "line4": "Locality", "line5": "Town", "postcode": "AB1 2CD"}
        },
        "offender": {"crn": "X12345", "pnc": "example-pnc", "cro": "example-cro"},
        "probation": {
          "probationStatus": "PREVIOUSLY_KNOWN",
          "awaitingPsr": false,
          "breach": false,
          "previouslyKnownTerminationDate": "2025-03-04",
          "preSentenceActivity": true
        },
        "groupedOffenderMatches": {
          "matches": [
            {
              "matchIdentifiers": {
                "crn": "X12345",
                "pnc": "example-pnc",
                "cro": "example-cro",
                "aliases": [{"firstName": "Alias", "surname": "Example", "middleNames": ["Middle Names"]}]
              },
              "matchType": "NAME_DOB_ALIAS",
              "confirmed": false,
              "rejected": false
            }
          ]
        },
        "warnings": [],
        "errors": [],
        "enrichedBy": "court-case-enrichment-worker",
        "jobRunId": "job-run-uuid"
      }
    """
  }
}
