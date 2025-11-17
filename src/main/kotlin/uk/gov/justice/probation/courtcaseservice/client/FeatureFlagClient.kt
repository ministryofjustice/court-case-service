package uk.gov.justice.probation.courtcaseservice.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class FeatureFlagClient(private val webClient: WebClient) {

  fun getFeatureFlags(request: FeatureFlagRequest): Mono<FeatureFlagResponse> {
    val evaluationPath = "/evaluate/v1/boolean"
    return webClient.post()
      .uri(evaluationPath)
      .bodyValue(request)
      .retrieve()
      .bodyToMono(FeatureFlagResponse::class.java)
  }
}

data class FeatureFlagRequest(
  val namespaceKey: String = "ProbationInCourt",
  val entityId: String,
  val flagKey: String,
  val context: Map<String, String>? = null,
)

data class FeatureFlagResponse(
  val enabled: Boolean,
)
