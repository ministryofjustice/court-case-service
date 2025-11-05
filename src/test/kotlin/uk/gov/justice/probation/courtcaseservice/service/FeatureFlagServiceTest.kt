package uk.gov.justice.probation.courtcaseservice.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagClient
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse

@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
internal class FeatureFlagServiceTest {

  @Mock
  lateinit var featureFlagClient: FeatureFlagClient

  private lateinit var service: FeatureFlagService

  @BeforeEach
  fun setUp() {
    service = FeatureFlagService(featureFlagClient)
  }

  @Test
  fun `should return feature flag response from client`() {
    val flagKey = "test-flag"
    val context = mapOf("user" to "test-user")
    val expectedResponse = FeatureFlagResponse(enabled = true)
    val request = FeatureFlagRequest(
      namespaceKey = "ProbationInCourt",
      entityId = flagKey,
      flagKey = flagKey,
      context = context,
    )

    `when`(featureFlagClient.getFeatureFlags(request)).thenReturn(Mono.just(expectedResponse))

    val result = service.isFeatureEnabled(flagKey, context).block()

    assert(result == expectedResponse)
  }

  @Test
  fun `should return feature flag response from client without context`() {
    val flagKey = "test-flag"
    val expectedResponse = FeatureFlagResponse(enabled = false)
    val request = FeatureFlagRequest(
      namespaceKey = "ProbationInCourt",
      entityId = flagKey,
      flagKey = flagKey,
      context = null,
    )

    `when`(featureFlagClient.getFeatureFlags(request)).thenReturn(Mono.just(expectedResponse))

    val result = service.isFeatureEnabled(flagKey).block()

    assert(result == expectedResponse)
  }

  @Test
  fun `should evict featureFlags cache`() {
    // Just call the method to ensure it does not throw
    service.evictFeatureFlagsCache()
  }
}
