package uk.gov.justice.probation.courtcaseservice.service
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagClient
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse

@Service
class FeatureFlagService(
  private val featureFlagClient: FeatureFlagClient,
) {
  @Cacheable("featureFlags")
  fun isFeatureEnabled(flagKey: String, context: Map<String, String>? = null): Mono<FeatureFlagResponse> {
    val request = FeatureFlagRequest(
      entityId = flagKey,
      flagKey = flagKey,
      context = context,
    )
    return featureFlagClient.getFeatureFlags(request)
  }

  @Scheduled(cron = "0 */10 * * * *")
  @CacheEvict(value = ["featureFlags"], allEntries = true)
  fun evictFeatureFlagsCache() {
  }
}
