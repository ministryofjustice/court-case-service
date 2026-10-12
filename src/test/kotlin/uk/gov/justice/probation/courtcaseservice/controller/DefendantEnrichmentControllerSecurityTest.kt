package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.CommonPlatformEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.controller.model.LibraEnrichmentTargets
import uk.gov.justice.probation.courtcaseservice.security.OAuth2SecurityConfig
import uk.gov.justice.probation.courtcaseservice.service.DefendantEnrichmentService
import java.time.LocalDate

@WebMvcTest(DefendantEnrichmentController::class)
@ActiveProfiles("test")
@Import(OAuth2SecurityConfig::class, ClientDetails::class)
class DefendantEnrichmentControllerSecurityTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @MockitoBean
  lateinit var service: DefendantEnrichmentService

  @MockitoBean
  lateinit var jwtDecoder: JwtDecoder

  @MockitoBean
  lateinit var clientRegistrationRepository: ClientRegistrationRepository

  @MockitoBean
  lateinit var jpaMappingContext: JpaMetamodelMappingContext

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should accept a service token without user claims`(endpoint: String) {
    given(jwtDecoder.decode("worker-token")).willReturn(serviceToken(listOf("ROLE_PREPARE_A_CASE")))
    val fromDate = LocalDate.of(2026, 10, 9)
    val toDate = LocalDate.of(2026, 10, 23)
    if (endpoint == "defendant-ids") {
      given(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, null, 0, 200))
        .willReturn(CommonPlatformEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))
    } else {
      given(service.getLibraEnrichmentCandidates(fromDate, toDate, null, 0, 200))
        .willReturn(LibraEnrichmentTargets(emptyList(), 0, 200, 0L, 0, false))
    }

    mockMvc.perform(request(endpoint).header("Authorization", "Bearer worker-token"))
      .andExpect(status().isOk)
  }

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should return 401 without a token`(endpoint: String) {
    mockMvc.perform(request(endpoint)).andExpect(status().isUnauthorized)
  }

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should return 401 for an invalid token`(endpoint: String) {
    given(jwtDecoder.decode("invalid-token")).willThrow(BadJwtException("Invalid token"))

    mockMvc.perform(request(endpoint).header("Authorization", "Bearer invalid-token"))
      .andExpect(status().isUnauthorized)
  }

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should return 403 for a service token without permission`(endpoint: String) {
    given(jwtDecoder.decode("worker-token")).willReturn(serviceToken(emptyList()))

    mockMvc.perform(request(endpoint).header("Authorization", "Bearer worker-token"))
      .andExpect(status().isForbidden)
  }

  @ParameterizedTest
  @ValueSource(strings = ["defendant-ids", "cids"])
  fun `should propagate service failures as 5xx rather than empty results`(endpoint: String) {
    given(jwtDecoder.decode("worker-token")).willReturn(serviceToken(listOf("ROLE_PREPARE_A_CASE")))
    val fromDate = LocalDate.of(2026, 10, 9)
    val toDate = LocalDate.of(2026, 10, 23)
    val failure = ResponseStatusException(SERVICE_UNAVAILABLE, "Database unavailable")
    if (endpoint == "defendant-ids") {
      given(service.getCommonPlatformEnrichmentCandidates(fromDate, toDate, null, 0, 200)).willThrow(failure)
    } else {
      given(service.getLibraEnrichmentCandidates(fromDate, toDate, null, 0, 200)).willThrow(failure)
    }

    mockMvc.perform(request(endpoint).header("Authorization", "Bearer worker-token"))
      .andExpect(status().isServiceUnavailable)
      .andExpect(content().string(""))
  }

  private fun request(endpoint: String): MockHttpServletRequestBuilder =
    get("/enrichment-targets/$endpoint")
      .param("fromDate", "2026-10-09").param("toDate", "2026-10-23")
      .param("page", "0").param("size", "200")

  private fun serviceToken(authorities: List<String>): Jwt = Jwt.withTokenValue("worker-token")
    .header("alg", "RS256")
    .subject("enrichment-worker")
    .claim("client_id", "enrichment-worker")
    .claim("authorities", authorities)
    .build()
}
