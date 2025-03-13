package uk.gov.justice.probation.courtcaseservice.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component

@Component
class HealthInfo(buildProperties: BuildProperties) : HealthIndicator {
  private val version: String = buildProperties.version

  override fun health(): Health = Health.up().withDetail("version", version).build()
}
