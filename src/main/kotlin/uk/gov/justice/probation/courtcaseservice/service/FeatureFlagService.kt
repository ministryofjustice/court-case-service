package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.EvaluationRequest
import uk.gov.justice.probation.courtcaseservice.client.FliptRestClient

@Service
class FeatureFlagService(val fliptRestClient: FliptRestClient) {
  @JvmOverloads
  fun evaluateCourtCode(code: String, flagKey: String = "pic-test"): Mono<Boolean> {
        val namespaceKey = "ProbationInCourt"
        val entityId = code
        val context = mapOf<String, String>("code" to code)
        val evaluationRequest = EvaluationRequest(namespaceKey, flagKey, entityId, context)
        return fliptRestClient.evaluate(evaluationRequest)
            .map {value -> value.enabled }
    }
}
