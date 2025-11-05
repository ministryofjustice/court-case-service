package uk.gov.justice.probation.courtcaseservice.controller

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagRequest
import uk.gov.justice.probation.courtcaseservice.client.FeatureFlagResponse
import uk.gov.justice.probation.courtcaseservice.service.FeatureFlagService
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper

@WebFluxTest(FeatureFlagController::class)
@TestPropertySource(
  properties = [
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
  ],
)
internal class FeatureFlagControllerIntTest {

  companion object {
    private const val FLAG_KEY = "prepare-a-case-v2"
    private const val ENTITY_ID = "prepare-a-case"
  }

  @MockBean
  lateinit var featureFlagService: FeatureFlagService

  @BeforeEach
  fun initTest() {
    given(featureFlagService.isFeatureEnabled(any(), any()))
      .willReturn(Mono.just(FeatureFlagResponse(enabled = true)))
  }

  @Test
  fun `given valid flag request should return feature enabled true`() {
    val requestBody = FeatureFlagRequest(
      flagKey = FLAG_KEY,
      entityId = ENTITY_ID,
      context = mapOf("code" to "B22KS"),
    )

    val response =
      given()
        .auth()
        .oauth2(TokenHelper.getToken())
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(requestBody)
        .`when`()
        .post("/feature-flags/evaluate")
        .then()
        .statusCode(200)
        .extract()
        .`as`(FeatureFlagResponse::class.java)

    assertThat(response.enabled)
      .describedAs("Feature flag $FLAG_KEY should be enabled for this court code")
      .isIn(true, false)

    verify(featureFlagService)
      .isFeatureEnabled(FLAG_KEY, mapOf("code" to "B22KS"))
  }

  @Test
  fun `given invalid flag key should return 400`() {
    val invalidRequest = mapOf(
      "flagKey" to "non-existent-flag",
      "entityId" to ENTITY_ID,
      "context" to mapOf("code" to "B22KS"),
    )

    given()
      .auth()
      .oauth2(TokenHelper.getToken())
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body(invalidRequest)
      .`when`()
      .post("/feature-flags/evaluate")
      .then()
      .statusCode(400)
  }
}
