package uk.gov.justice.probation.courtcaseservice.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.probation.courtcaseservice.BaseIntTest

/**
 * Sample test to check the service implementation is picked up by the endpoint and the service access request endpoint
 * is created.
 *
 * Also see SubjectAccessRequestServiceSampleTest for a sample service implementation.
 */
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-SubjectAccessRequestIntTest.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED)
)
@Sql(
  scripts = ["classpath:after-test.sql"],
  config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
class SubjectAccessRequestSampleIntegrationTest : BaseIntTest() {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  internal fun setAuthorisation(
    user: String = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)

  @Nested
  @DisplayName("/subject-access-request")
  inner class SubjectAccessRequestEndpoint {
    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `should return data if crn exists`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?crn=X25829")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.comments[0].comment").isEqualTo("PSR in progress")
          .jsonPath("$.content.comments[0].author").isEqualTo("Author One")
          .jsonPath("$.content.comments[0].created").isEqualTo("2024-05-21T09:45:55.597")
          .jsonPath("$.content.comments[0].createdBy").isEqualTo("before-test.sql")
          .jsonPath("$.content.comments[0].lastUpdated").isEqualTo("2024-04-08T09:45:55.597")
          .jsonPath("$.content.comments[0].lastUpdatedBy").isEqualTo("Last Updated Author")
          .jsonPath("$.content.comments[0].caseNumber").isEqualTo("1600028888")
      }

      @Test
      fun `should return data for additional TEST_DATA_ACCESS role`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_TEST_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.prisonerNumber").isEqualTo("A12345")
          .jsonPath("$.content.commentText").isEqualTo("some useful comment")
      }
    }
  }
}