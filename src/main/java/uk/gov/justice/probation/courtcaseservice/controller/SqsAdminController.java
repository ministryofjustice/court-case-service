package uk.gov.justice.probation.courtcaseservice.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.service.SqsAdminService;

@RestController
@RequestMapping("/queue-admin")
public class SqsAdminController {

    private final SqsAdminService sqsAdminService;

    public SqsAdminController(SqsAdminService sqsAdminService) {
        this.sqsAdminService = sqsAdminService;
    }

    @PutMapping("/retry-probationOffenderEvents-dlq")
    public void replayDlqMessages() {
        sqsAdminService.retryProbationOffenderEventsDlqMessages();
    }

}
