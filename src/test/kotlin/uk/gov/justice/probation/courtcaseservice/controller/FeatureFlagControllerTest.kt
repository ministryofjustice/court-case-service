package uk.gov.justice.probation.courtcaseservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse
import uk.gov.justice.probation.courtcaseservice.service.FeatureFlagService

@ExtendWith(MockitoExtension::class)
internal class FeatureFlagControllerTest {

  companion object {
    private const val FLAG_KEY = "prepare-a-case-v2"
    private const val ENTITY_ID = "prepare-a-case"
    private val CONTEXT = mapOf("code" to "B22KS")
  }

  @Mock
  lateinit var featureFlagService: FeatureFlagService

  @InjectMocks
  lateinit var featureFlagController: FeatureFlagController

  @Test
  fun `should call service and return feature enabled true`() {
    val request = FeatureFlagRequest(
      flagKey = FLAG_KEY,
      entityId = ENTITY_ID,
      context = CONTEXT,
    )
    given(featureFlagService.isFeatureEnabled(FLAG_KEY, CONTEXT))
      .willReturn(Mono.just(FeatureFlagResponse(enabled = true)))

    val response = featureFlagController.evaluateFeatureFlag(request).block()

    verify(featureFlagService).isFeatureEnabled(FLAG_KEY, CONTEXT)
    assertThat(response!!.enabled).isTrue()
  }

  @Test
  fun `should call service and return feature disabled`() {
    val request = FeatureFlagRequest(
      flagKey = FLAG_KEY,
      entityId = ENTITY_ID,
      context = CONTEXT,
    )
    given(featureFlagService.isFeatureEnabled(FLAG_KEY, CONTEXT))
      .willReturn(Mono.just(FeatureFlagResponse(enabled = false)))

    val response = featureFlagController.evaluateFeatureFlag(request).block()

    verify(featureFlagService).isFeatureEnabled(FLAG_KEY, CONTEXT)
    assertThat(response!!.enabled).isFalse()
  }

  @Test
  fun `should handle null context gracefully`() {
    val request = FeatureFlagRequest(
      flagKey = FLAG_KEY,
      entityId = ENTITY_ID,
      context = null,
    )
    given(featureFlagService.isFeatureEnabled(FLAG_KEY, null))
      .willReturn(Mono.just(FeatureFlagResponse(enabled = true)))

    val response = featureFlagController.evaluateFeatureFlag(request).block()

    verify(featureFlagService).isFeatureEnabled(FLAG_KEY, null)
    assertThat(response!!.enabled).isTrue()
  }
}
