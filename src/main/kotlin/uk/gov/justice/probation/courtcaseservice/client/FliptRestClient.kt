package uk.gov.justice.probation.courtcaseservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class FliptRestClient(
  @param:Value("\${flipt.flipt-url}") private val fliptUrl: String,
  @param:Value("\${flipt.flipt-token}") private val fliptToken: String ) {

  private val evaluationPath = "/ap1/v1/evaluate"

  val webClient = WebClient.builder()
    .baseUrl(fliptUrl + evaluationPath )
    .defaultHeader("Authorization", "Bearer $fliptToken")
    .defaultHeader("Content-Type", "application/json")
    .build()

  fun evaluate(request: EvaluationRequest): Mono<EvaluationResponse> {
    return webClient.post()
      .bodyValue(request)
      .retrieve()
      .bodyToMono(EvaluationResponse::class.java)
  }
}

data class EvaluationRequest(
  val namespaceKey: String = "ProbationInCourt",
  val flagKey: String,
  val entityId: String,
  val context: Map<String, String>
)

data class EvaluationResponse(
  val enabled: Boolean
)

