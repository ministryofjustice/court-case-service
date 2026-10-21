package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails
import uk.gov.justice.probation.courtcaseservice.security.OAuth2SecurityConfig
import uk.gov.justice.probation.courtcaseservice.service.EnrichmentResultService

@WebMvcTest(EnrichmentResultController::class)
@ActiveProfiles("test")
@Import(OAuth2SecurityConfig::class, ClientDetails::class)
class EnrichmentResultControllerSecurityTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @MockitoBean
  lateinit var service: EnrichmentResultService

  @MockitoBean
  lateinit var jwtDecoder: JwtDecoder

  @MockitoBean
  lateinit var clientRegistrationRepository: ClientRegistrationRepository

  @MockitoBean
  lateinit var jpaMappingContext: JpaMetamodelMappingContext

  @Test
  fun `should accept a service token with the prepare a case role`() {
    given(jwtDecoder.decode("worker-token")).willReturn(serviceToken(listOf("ROLE_PREPARE_A_CASE")))

    mockMvc.perform(request().header("Authorization", "Bearer worker-token"))
      .andExpect(status().isNoContent)
  }

  @Test
  fun `should return 401 without a token`() {
    mockMvc.perform(request()).andExpect(status().isUnauthorized)
    verifyNoInteractions(service)
  }

  @Test
  fun `should return 401 for an invalid token`() {
    given(jwtDecoder.decode("invalid-token")).willThrow(BadJwtException("Invalid token"))

    mockMvc.perform(request().header("Authorization", "Bearer invalid-token"))
      .andExpect(status().isUnauthorized)
    verifyNoInteractions(service)
  }

  @Test
  fun `should return 403 for a service token without permission`() {
    given(jwtDecoder.decode("worker-token")).willReturn(serviceToken(emptyList()))

    mockMvc.perform(request().header("Authorization", "Bearer worker-token"))
      .andExpect(status().isForbidden)
    verifyNoInteractions(service)
  }

  private fun request(): MockHttpServletRequestBuilder = post("/enrichment-results")
    .contentType(APPLICATION_JSON)
    .content("""{"sourceType":"LIBRA","targetId":"c12345","status":"NO_MATCH","enrichedBy":"court-case-enrichment-worker"}""")

  private fun serviceToken(authorities: List<String>): Jwt = Jwt.withTokenValue("worker-token")
    .header("alg", "RS256")
    .subject("enrichment-worker")
    .claim("client_id", "enrichment-worker")
    .claim("authorities", authorities)
    .build()
}
