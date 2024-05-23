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
class SubjectAccessRequestIntegrationTest : BaseIntTest() {

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
    inner class SARContent {
      @Test
      fun `should return case comments, hearing outcomes and hearing notes if present for defendant`() {
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
          .jsonPath("$.content.comments[1].comment").isEqualTo("PSR in progress")
          .jsonPath("$.content.comments[1].author").isEqualTo("Author Three")
          .jsonPath("$.content.comments[1].created").isEqualTo("2024-04-21T09:45:55.597")
          .jsonPath("$.content.comments[1].createdBy").isEqualTo("before-test.sql")
          .jsonPath("$.content.comments[1].lastUpdated").isEqualTo("2024-03-08T09:45:55.597")
          .jsonPath("$.content.comments[1].lastUpdatedBy").isEqualTo("Last Updated Author3")
          .jsonPath("$.content.comments[1].caseNumber").isEqualTo("1600028888")
          .jsonPath("$.content.hearingOutcomes[0].outcomeType").isEqualTo("ADJOURNED")
          .jsonPath("$.content.hearingOutcomes[0].outcomeDate").isEqualTo("2023-04-24T09:09:09")
          .jsonPath("$.content.hearingOutcomes[0].resultedDate").isEqualTo("2023-04-25T09:09:09")
          .jsonPath("$.content.hearingOutcomes[0].state").isEqualTo("IN_PROGRESS")
          .jsonPath("$.content.hearingOutcomes[0].assignedTo").isEqualTo("John Smith")
          .jsonPath("$.content.hearingNotes[0].hearingId").isEqualTo("fe657c3a-b674-4e17-8772-7281c99e4f9f")
          .jsonPath("$.content.hearingNotes[0].note").isEqualTo("This is a test comment by the Prepare a case digital team.")
          .jsonPath("$.content.hearingNotes[0].author").isEqualTo("John Doe")
      }

      @Test
      fun `should return case comments if present for defendant but no case number if not LIBRA`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?crn=B25829")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.comments[0].comment").isEqualTo("PSR completed")
          .jsonPath("$.content.comments[0].author").isEqualTo("Author Two")
          .jsonPath("$.content.comments[0].created").isEqualTo("2024-05-22T09:45:55.597")
          .jsonPath("$.content.comments[0].createdBy").isEqualTo("before-test.sql")
          .jsonPath("$.content.comments[0].lastUpdated").isEqualTo("2024-04-09T09:45:55.597")
          .jsonPath("$.content.comments[0].lastUpdatedBy").isEqualTo("Last Updated Author2")
          .jsonPath("$.content.comments[0].caseNumber").isEqualTo("")
      }

      @Test
      fun `should not return case comments if not present for defendant`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?crn=Z258210")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content.comments").isEmpty
      }

      @Test
      fun `should return case comments if present for defendant between dates`() {
        // service will return data for prisoners that start with A
        webTestClient.get().uri("/subject-access-request?crn=X25829&fromDate=2024-04-21&toDate=2024-05-22")
        fun `should return case comments, hearing notes and hearing outcomes if between valid date ranges `() {
          // service will return data for prisoners that start with A
          webTestClient.get().uri("/subject-access-request?crn=B25829")
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
            .jsonPath("$.content.comments[1].comment").isEqualTo("PSR in progress")
            .jsonPath("$.content.comments[1].author").isEqualTo("Author Three")
            .jsonPath("$.content.comments[1].created").isEqualTo("2024-04-21T09:45:55.597")
            .jsonPath("$.content.comments[1].createdBy").isEqualTo("before-test.sql")
            .jsonPath("$.content.comments[1].lastUpdated").isEqualTo("2024-03-08T09:45:55.597")
            .jsonPath("$.content.comments[1].lastUpdatedBy").isEqualTo("Last Updated Author3")
            .jsonPath("$.content.comments[1].caseNumber").isEqualTo("1600028888")
        }

        @Test
        fun `should return case comments if present for defendant from date`() {
          // service will return data for prisoners that start with A
          webTestClient.get().uri("/subject-access-request?crn=X25829&fromDate=2024-05-21")
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
        fun `should return case comments if present for defendant to date`() {
          // service will return data for prisoners that start with A
          webTestClient.get().uri("/subject-access-request?crn=X25829&toDate=2024-04-25")
            .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.comments[0].comment").isEqualTo("PSR in progress")
            .jsonPath("$.content.comments[0].author").isEqualTo("Author Three")
            .jsonPath("$.content.comments[0].created").isEqualTo("2024-04-21T09:45:55.597")
            .jsonPath("$.content.comments[0].createdBy").isEqualTo("before-test.sql")
            .jsonPath("$.content.comments[0].lastUpdated").isEqualTo("2024-03-08T09:45:55.597")
            .jsonPath("$.content.comments[0].lastUpdatedBy").isEqualTo("Last Updated Author3")
            .jsonPath("$.content.comments[0].caseNumber").isEqualTo("1600028888")
            .jsonPath("$.content.comments[0].comment").isEqualTo("PSR completed")
            .jsonPath("$.content.comments[0].author").isEqualTo("Author Two")
            .jsonPath("$.content.comments[0].created").isEqualTo("2024-05-22T09:45:55.597")
            .jsonPath("$.content.comments[0].createdBy").isEqualTo("before-test.sql")
            .jsonPath("$.content.comments[0].lastUpdated").isEqualTo("2024-04-09T09:45:55.597")
            .jsonPath("$.content.comments[0].lastUpdatedBy").isEqualTo("Last Updated Author2")
            .jsonPath("$.content.comments[0].caseNumber").isEqualTo("")
            .jsonPath("$.content.hearingOutcomes[0].hearingId").isEqualTo("-198")
            .jsonPath("$.content.hearingOutcomes[0].outcomeType").isEqualTo("ADJOURNED")
            .jsonPath("$.content.hearingOutcomes[0].outcomeDate").isEqualTo("2023-04-24T09:09:09")
            .jsonPath("$.content.hearingOutcomes[0].resultedDate").isEqualTo("2023-04-25T09:09:09")
            .jsonPath("$.content.hearingOutcomes[0].state").isEqualTo("IN_PROGRESS")
            .jsonPath("$.content.hearingOutcomes[0].assignedTo").isEqualTo("John Smith")
            .jsonPath("$.content.hearingNotes[0].hearingId").isEqualTo("fe657c3a-b674-4e17-8772-7281c99e4f9f")
            .jsonPath("$.content.hearingNotes[0].note")
            .isEqualTo("This is a test comment by the Prepare a case digital team.")
            .jsonPath("$.content.hearingNotes[0].author").isEqualTo("John Doe")
        }
      }
    }
  }
}