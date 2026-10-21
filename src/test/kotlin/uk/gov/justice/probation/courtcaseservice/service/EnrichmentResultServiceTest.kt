package uk.gov.justice.probation.courtcaseservice.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedAddress
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedAlias
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedDefendantDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedGroupedOffenderMatches
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedMatchIdentifiers
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedName
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedOffenderDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedOffenderMatch
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentSourceType
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentStatus
import java.time.LocalDate

class EnrichmentResultServiceTest {
  private val appender = ListAppender<ILoggingEvent>()
  private val logger = LoggerFactory.getLogger(EnrichmentResultService::class.java) as Logger

  @BeforeEach
  fun setUp() {
    appender.start()
    logger.addAppender(appender)
  }

  @AfterEach
  fun tearDown() {
    logger.detachAppender(appender)
  }

  @Test
  fun `should log operational metadata and counts without personal data`() {
    EnrichmentResultService().persistEnrichmentResult(
      EnrichmentResultRequest(
        sourceType = EnrichmentSourceType.COMMON_PLATFORM,
        targetId = "defendant-id",
        status = EnrichmentStatus.NEEDS_REVIEW,
        defendant = EnrichedDefendantDetails(
          cprUUID = "person-record-id",
          name = EnrichedName(forename1 = "Alex", surname = "Example"),
          dateOfBirth = LocalDate.of(1990, 1, 2),
          address = EnrichedAddress(line1 = "Building", postcode = "AB1 2CD"),
        ),
        offender = EnrichedOffenderDetails(crn = "X12345", pnc = "example-pnc", cro = "example-cro"),
        groupedOffenderMatches = EnrichedGroupedOffenderMatches(
          listOf(
            EnrichedOffenderMatch(
              EnrichedMatchIdentifiers(crn = "X12345", aliases = listOf(EnrichedAlias(firstName = "Alias"))),
              rejected = true,
            ),
            EnrichedOffenderMatch(EnrichedMatchIdentifiers(crn = "X67890")),
          ),
        ),
        warnings = listOf("warning text"),
        enrichedBy = "court-case-enrichment-worker",
        jobRunId = "job-run-uuid",
      ),
    )

    val message = appender.list.single().formattedMessage
    assertThat(message)
      .contains("sourceType=COMMON_PLATFORM", "targetId=defendant-id", "status=NEEDS_REVIEW", "jobRunId=job-run-uuid")
      .contains("hasDefendant=true", "hasOffender=true", "hasProbation=false")
      .contains("matchCount=2", "confirmedMatchCount=0", "rejectedMatchCount=1", "warningCount=1", "errorCount=0")
      .doesNotContain("person-record-id", "Alex", "Example", "1990", "AB1 2CD", "X12345", "X67890", "example-pnc", "example-cro", "Alias", "warning text")
  }
}
