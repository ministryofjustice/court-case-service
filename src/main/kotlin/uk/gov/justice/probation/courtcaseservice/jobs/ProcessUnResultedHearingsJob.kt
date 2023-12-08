package uk.gov.justice.probation.courtcaseservice.jobs

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.service.CaseWorkflowService
import java.time.Clock

@Component
@Slf4j
class ProcessUnResultedHearingsJob(val caseWorkflowService: CaseWorkflowService, val clock: Clock) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "\${hearing_outcomes.process_un_resulted.cron:0 30 18 * * ?}")
    fun schedule() {
        logger.info("Running job move un resulted hearings to outcomes flow")
        this.caseWorkflowService.processUnResultedCases()
    }
}