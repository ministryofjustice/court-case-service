package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse
import uk.gov.justice.probation.courtcaseservice.service.FeatureFlagService

@ExtendWith(MockitoExtension::class)
internal class FeatureFlagControllerTest {

  private val featureFlagService = mock(FeatureFlagService::class.java)
  private val controller = FeatureFlagController(featureFlagService)
  private val webTestClient = WebTestClient.bindToController(controller).build()

  @Test
  fun `should return enabled true when service returns true`() {
    // Given
    val request = FeatureFlagRequest(
      entityId = "prepare-a-case",
      flagKey = "prepare-a-case-v2",
      context = mapOf("code" to "B22KS"),
    )

    val expectedResponse = FeatureFlagResponse(enabled = true)
    `when`(
      featureFlagService.isFeatureEnabled(
        eq(request.flagKey),
        eq(request.context),
      ),
    ).thenReturn(Mono.just(expectedResponse))

    // When + Then
    webTestClient.post()
      .uri("/feature-flags/evaluate")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.enabled").isEqualTo(true)

    verify(featureFlagService).isFeatureEnabled(request.flagKey, request.context)
  }
}
