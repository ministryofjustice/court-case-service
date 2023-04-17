package uk.gov.justice.probation.courtcaseservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@EnableCaching
@EnableJpaRepositories("uk.gov.justice.probation.courtcaseservice.jpa.repository")
class CourtCaseServiceApplication

fun main(args: Array<String>) {
    runApplication<CourtCaseServiceApplication>(*args)
}
