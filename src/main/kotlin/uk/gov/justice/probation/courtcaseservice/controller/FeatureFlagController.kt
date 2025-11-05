package uk.gov.justice.probation.courtcaseservice.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.service.FeatureFlagService

@RestController
@RequestMapping("/feature-flags")
class FeatureFlagController(
  private val featureFlagService: FeatureFlagService
) {

  @PostMapping("/evaluate")
  fun evaluateFeatureFlag(
    @RequestBody body: FeatureFlagRequest
  ): Mono<FeatureFlagResponse> {
    return featureFlagService.isFeatureEnabled(
      flagKey = body.flagKey,
      entityId = body.entityId,
      context = body.context
    )
  }
}