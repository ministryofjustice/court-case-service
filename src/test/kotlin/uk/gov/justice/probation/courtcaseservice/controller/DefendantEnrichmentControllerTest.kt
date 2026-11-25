package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.service.DefendantEnrichmentService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DefendantEnrichmentControllerTest {
  @Mock
  lateinit var service: DefendantEnrichmentService

  lateinit var mockMvc: MockMvc

  @BeforeEach
  fun setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(DefendantEnrichmentController(service)).build()
  }

  @Test
  fun `should return all common platform IDs without pagination metadata`() {
    given(service.getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, COURT))
      .willReturn(CommonPlatformEnrichmentTargets(listOf("id-1", "id-2")))

    mockMvc.perform(validRequest("defendant-ids"))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(content().json("""{"defendantIds":["id-1","id-2"]}""", true))

    verify(service).getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, COURT)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun `should return all libra IDs without pagination metadata`() {
    given(service.getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, COURT))
      .willReturn(LibraEnrichmentTargets(listOf("c12345", "c67890")))

    mockMvc.perform(validRequest("cids"))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(content().json("""{"cIds":["c12345","c67890"]}""", true))

    verify(service).getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, COURT)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun `should return empty lists`() {
    given(service.getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, COURT))
      .willReturn(CommonPlatformEnrichmentTargets(emptyList()))
    given(service.getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, COURT))
      .willReturn(LibraEnrichmentTargets(emptyList()))

    mockMvc.perform(validRequest("defendant-ids")).andExpect(content().json("""{"defendantIds":[]}""", true))
    mockMvc.perform(validRequest("cids")).andExpect(content().json("""{"cIds":[]}""", true))
  }

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should accept ranges longer than 14 days`(endpoint: String) {
    val laterDate = LocalDate.of(2026, 11, 23)
    if (endpoint == "defendant-ids") {
      given(service.getCommonPlatformEnrichmentCandidates(FROM_DATE, laterDate, COURT))
        .willReturn(CommonPlatformEnrichmentTargets(emptyList()))
    } else {
      given(service.getLibraEnrichmentCandidates(FROM_DATE, laterDate, COURT))
        .willReturn(LibraEnrichmentTargets(emptyList()))
    }

    mockMvc.perform(
      get("/enrichment-targets/$endpoint")
        .param("fromDate", FROM_DATE.toString()).param("toDate", laterDate.toString()).param("courtCode", COURT),
    ).andExpect(status().isOk)

    if (endpoint == "defendant-ids") {
      verify(service).getCommonPlatformEnrichmentCandidates(FROM_DATE, laterDate, COURT)
    } else {
      verify(service).getLibraEnrichmentCandidates(FROM_DATE, laterDate, COURT)
    }
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  fun `should reject invalid or missing parameters before calling the service`(request: MockHttpServletRequestBuilder) {
    mockMvc.perform(request).andExpect(status().isBadRequest)
    verifyNoMoreInteractions(service)
  }

  companion object {
    private val FROM_DATE = LocalDate.of(2026, 10, 9)
    private val TO_DATE = LocalDate.of(2026, 10, 23)
    private const val COURT = "B10JQ"

    private fun validRequest(endpoint: String): MockHttpServletRequestBuilder = get("/enrichment-targets/$endpoint")
      .param("fromDate", FROM_DATE.toString()).param("toDate", TO_DATE.toString()).param("courtCode", COURT)

    @JvmStatic
    fun invalidRequests(): List<MockHttpServletRequestBuilder> {
      val valid = mapOf("fromDate" to "2026-10-09", "toDate" to "2026-10-23", "courtCode" to COURT)
      val invalidParameters = valid.keys.map { valid - it } + listOf(
        valid + ("fromDate" to "not-a-date"),
        valid + ("fromDate" to "2026-02-30"),
        valid + ("toDate" to "2026-10-09"),
        valid + ("toDate" to "2026-10-08"),
        valid + ("courtCode" to ""),
        valid + ("courtCode" to "  "),
        valid + ("courtCode" to "B10JQ,B14LO"),
      )
      return listOf("defendant-ids", "cids").flatMap { endpoint ->
        invalidParameters.map { parameters ->
          get("/enrichment-targets/$endpoint").apply {
            parameters.forEach { (name, value) -> param(name, value) }
          }
        } + validRequest(endpoint).param("courtCode", "B14LO")
      }
    }
  }
}
