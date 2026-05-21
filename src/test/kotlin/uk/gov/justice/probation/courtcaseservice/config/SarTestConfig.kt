package uk.gov.justice.probation.courtcaseservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.JwtDecoder
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@TestConfiguration
open class SarTestConfig {

  private val jwtAuthorisationHelper = JwtAuthorisationHelper()

  @Bean
  @Primary
  open fun sarJwtDecoder(): JwtDecoder = jwtAuthorisationHelper.jwtDecoder()

  @Bean
  open fun sarIntegrationTestHelper(
    @Value("\${hmpps.sar.tests.expected-api-response.path:}") expectedApiResponsePath: String,
    @Value("\${hmpps.sar.tests.expected-render-result.path:}") expectedRenderResultPath: String,
    @Value("\${hmpps.sar.tests.attachments-expected:false}") attachmentsExpected: Boolean,
    @Value("\${hmpps.sar.tests.expected-flyway-schema-version:0}") expectedFlywaySchemaVersion: String,
    @Value("\${hmpps.sar.tests.expected-jpa-entity-schema.path:}") expectedJpaEntitySchemaPath: String,
  ): SarIntegrationTestHelper = SarIntegrationTestHelper(
    jwtAuthHelper = jwtAuthorisationHelper,
    expectedApiResponsePath = expectedApiResponsePath,
    expectedRenderResultPath = expectedRenderResultPath,
    attachmentsExpected = attachmentsExpected,
    expectedFlywaySchemaVersion = expectedFlywaySchemaVersion,
    expectedJpaEntitySchemaPath = expectedJpaEntitySchemaPath,
  )
}
