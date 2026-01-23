package uk.gov.justice.probation.courtcaseservice.database.seeders.framework

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("seeding")
class SeedDatabaseCommand(
  private val seeders: List<Seeder>,
) : CommandLineRunner {
  @Transactional
  override fun run(vararg args: String?) {
    seeders.forEach { it.run() }
  }
}
