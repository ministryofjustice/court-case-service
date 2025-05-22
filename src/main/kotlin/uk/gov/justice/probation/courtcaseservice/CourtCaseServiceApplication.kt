package uk.gov.justice.probation.courtcaseservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@EnableCaching
class CourtCaseServiceApplication

fun main(args: Array<String>) {
  runApplication<CourtCaseServiceApplication>(*args)
}
