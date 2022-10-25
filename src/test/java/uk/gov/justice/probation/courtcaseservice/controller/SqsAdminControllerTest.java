package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.service.SqsAdminService;


import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsAdminControllerTest {

    @Mock
    private SqsAdminService sqsAdminService;

    @InjectMocks
    private SqsAdminController sqsAdminController;

    @BeforeEach
    void prepare() {
        sqsAdminController = new SqsAdminController(sqsAdminService);
    }

    @Test
    void shouldInvokeSqsAdminService() {
        sqsAdminController.replayDlqMessages();
        verify(sqsAdminService).retryProbationOffenderEventsDlqMessages();
    }
}