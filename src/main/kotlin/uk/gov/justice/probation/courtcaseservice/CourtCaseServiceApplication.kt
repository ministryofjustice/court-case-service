package uk.gov.justice.probation.courtcaseservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
open class CourtCaseServiceApplication

fun main(args: Array<String>) {
    runApplication<CourtCaseServiceApplication>(*args)
}
