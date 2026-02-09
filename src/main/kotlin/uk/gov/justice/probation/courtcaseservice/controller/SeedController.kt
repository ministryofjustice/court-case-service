package uk.gov.justice.probation.courtcaseservice.controller

import org.slf4j.Logger
import java.util.Optional
import java.time.LocalDate
import org.slf4j.LoggerFactory
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.probation.courtcaseservice.controller.model.SeedResponse
import uk.gov.justice.probation.courtcaseservice.database.seeders.CourtCaseSeeder

@RestController
class SeedController(
  private val courtCaseSeeder: CourtCaseSeeder,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(description = "Seeds the database with test data. Must be enabled in config file. Only runs in local/dev/test environments.")
  @PostMapping(value = ["/db-seed"], produces = [APPLICATION_JSON_VALUE])
  @Transactional
  fun seedDatabase(
    @RequestParam count: Optional<Int>,
    @RequestParam start: Optional<String>,
    @RequestParam days: Optional<Int>,
    @RequestParam court: Optional<String>,
  ) : SeedResponse {
    val c = if (count.isPresent) count.get().coerceIn(1, 500) else 1
    val d = if (days.isPresent) days.get().coerceIn(1, 30) else 1
    val s = if (start.isPresent) LocalDate.parse(start.get()) else LocalDate.now()

    // todo: allow the seeder to be run from a scheduled job
    courtCaseSeeder.options(count = c, start = s, days = d, court=court).run()
    log.info(
      "Seeded database via /db-seed with count={}, start={}, days={}, court={}",
      c,
      s,
      d,
      court.orElse("unspecified")
    )

    return SeedResponse(message = "Success.")
  }
}
