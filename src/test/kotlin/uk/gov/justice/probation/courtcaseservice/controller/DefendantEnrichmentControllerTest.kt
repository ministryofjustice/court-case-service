package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
  fun `should return the common platform page with all metadata`() {
    given(service.getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, "B10JQ", 0, 200))
      .willReturn(CommonPlatformEnrichmentTargets(listOf("cp-defendant-id"), 0, 200, 340L, 2, true))

    mockMvc.perform(validRequest("defendant-ids").param("courtCode", "B10JQ"))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(content().json("""{"defendantIds":["cp-defendant-id"],"page":0,"size":200,"totalElements":340,"totalPages":2,"hasNext":true}""", true))

    verify(service).getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, "B10JQ", 0, 200)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun `should return the libra page with case sensitive cIds and all metadata`() {
    given(service.getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, null, 0, 200))
      .willReturn(LibraEnrichmentTargets(listOf("c12345"), 0, 200, 95L, 1, false))

    mockMvc.perform(validRequest("cids"))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(content().json("""{"cIds":["c12345"],"page":0,"size":200,"totalElements":95,"totalPages":1,"hasNext":false}""", true))

    verify(service).getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, null, 0, 200)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun `should return an empty common platform page`() {
    given(service.getCommonPlatformEnrichmentCandidates(FROM_DATE, TO_DATE, null, 0, 200))
      .willReturn(CommonPlatformEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))

    mockMvc.perform(validRequest("defendant-ids"))
      .andExpect(status().isOk)
      .andExpect(content().json("""{"defendantIds":[],"page":0,"size":200,"totalElements":0,"totalPages":0,"hasNext":false}""", true))
  }

  @Test
  fun `should return an empty libra page`() {
    given(service.getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, null, 0, 200))
      .willReturn(LibraEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))

    mockMvc.perform(validRequest("cids"))
      .andExpect(status().isOk)
      .andExpect(content().json("""{"cIds":[],"page":0,"size":200,"totalElements":0,"totalPages":0,"hasNext":false}""", true))
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  fun `should reject invalid or missing parameters before calling the service`(request: MockHttpServletRequestBuilder) {
    mockMvc.perform(request).andExpect(status().isBadRequest)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun `should accept a later page and maximum page size`() {
    given(service.getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, null, 2, 500))
      .willReturn(LibraEnrichmentTargets(emptyList(), 2, 500, 0L, 0, false))

    mockMvc.perform(
      get("/enrichment-targets/cids")
        .param("fromDate", FROM_DATE.toString()).param("toDate", TO_DATE.toString())
        .param("page", "2").param("size", "500"),
    ).andExpect(status().isOk)

    verify(service).getLibraEnrichmentCandidates(FROM_DATE, TO_DATE, null, 2, 500)
  }

  companion object {
    private val FROM_DATE = LocalDate.of(2026, 10, 9)
    private val TO_DATE = LocalDate.of(2026, 10, 23)

    private fun validRequest(endpoint: String): MockHttpServletRequestBuilder = get("/enrichment-targets/$endpoint")
      .param("fromDate", FROM_DATE.toString()).param("toDate", TO_DATE.toString())
      .param("page", "0").param("size", "200")

    @JvmStatic
    fun invalidRequests(): List<MockHttpServletRequestBuilder> {
      val valid = mapOf("fromDate" to "2026-10-09", "toDate" to "2026-10-23", "page" to "0", "size" to "200")
      val invalidParameters = valid.keys.map { valid - it } + listOf(
        valid + ("fromDate" to "not-a-date"),
        valid + ("fromDate" to "2026-02-30"),
        valid + ("toDate" to "2026-10-09"),
        valid + ("toDate" to "2026-10-08"),
        valid + ("page" to "-1"),
        valid + ("page" to "not-an-integer"),
        valid + ("size" to "0"),
        valid + ("size" to "501"),
        valid + ("size" to "not-an-integer"),
      )
      return listOf("defendant-ids", "cids").flatMap { endpoint ->
        invalidParameters.map { parameters ->
          get("/enrichment-targets/$endpoint").apply {
            parameters.forEach { (name, value) -> param(name, value) }
          }
        }
      }
    }
  }
}
