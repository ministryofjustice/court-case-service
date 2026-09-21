package uk.gov.justice.probation.courtcaseservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.probation.courtcaseservice.controller.model.SeedResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.SeedScenarioDocument
import uk.gov.justice.probation.courtcaseservice.database.seeders.CourtCaseSeeder
import uk.gov.justice.probation.courtcaseservice.database.seeders.ScenarioSeedService
import java.time.LocalDate
import java.util.Optional

@RestController
class SeedController(
  private val courtCaseSeeder: CourtCaseSeeder,
  private val scenarioSeedService: ScenarioSeedService,
  private val objectMapper: ObjectMapper,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(description = "Seeds the database with test data. Must be enabled in config file. Only runs in local/dev environments.")
  @PostMapping(value = ["/db-seed"], produces = [APPLICATION_JSON_VALUE])
  @Transactional
  fun seedDatabase(
    @RequestParam count: Optional<Int>,
    @RequestParam start: Optional<String>,
    @RequestParam days: Optional<Int>,
    @RequestParam court: Optional<String>,
    @RequestParam clean: Optional<String>,
  ): SeedResponse {
    val c = if (count.isPresent) count.get().coerceIn(1, 500) else 1
    val d = if (days.isPresent) days.get().coerceIn(1, 30) else 1
    val s = if (start.isPresent) LocalDate.parse(start.get()) else LocalDate.now()
    val courtCode = if (court.isPresent) court.get() else "B10JQ"
    val shouldClean = clean.isPresent && clean.get().toBoolean()

    // todo: allow the seeder to be run from a scheduled job
    courtCaseSeeder.options(count = c, start = s, days = d, court = courtCode, shouldClean = shouldClean).run()
    log.info("Seeded database with count={}, start={}, days={}, court={}", c, s, d, courtCode)

    return SeedResponse(
      message = "Success.",
      details = "Generated ${c} case(s).",
    )
  }

  @Operation(description = "Seeds the database with a fully-specified QA scenario document. Only runs in local/dev environments.")
  @PostMapping(value = ["/db-seed/scenario"], produces = [APPLICATION_JSON_VALUE])
  @Transactional
  fun seedScenario(@RequestBody(required = false) body: String?): ResponseEntity<SeedResponse> {
    val json = body?.takeIf(String::isNotBlank) ?: return ResponseEntity.badRequest().body(
      SeedResponse(
        message = "Invalid scenario payload.",
        details = "Request body must be valid JSON.",
      ),
    )

    log.info("Received scenario seed payload, bytes={}", json.toByteArray().size)
    val scenario = runCatching { objectMapper.readValue(json, SeedScenarioDocument::class.java) }
      .getOrElse {
        log.warn("Failed to parse scenario seed payload", it)
        return ResponseEntity.badRequest().body(
          SeedResponse(
            message = "Invalid scenario payload.",
            details = "Request body must be valid JSON.",
          ),
        )
      }

    scenarioSeedService.seed(scenario)
    log.info("Seeded database from scenario document with {} case(s)", scenario.cases.size)
    return ResponseEntity.ok(
      SeedResponse(
        message = "Success.",
        details = "Loaded ${scenario.cases.size} case(s).",
      ),
    )
  }
}
