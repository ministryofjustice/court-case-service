package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.List.of;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsAdminServiceTest {

    private final static String PROBATION_OFFENDER_EVENT_SQS_DLQ_URL = "http://dlq";
    private final static String PROBATION_OFFENDER_EVENT_SQS_URL = "http://queue";

    @Mock
    private AmazonSQSAsync probationOffenderEventsDlq;

    @Mock
    private AmazonSQSAsync probationOffenderEventsQueue;

    private SqsAdminService sqsAdminService;

    @BeforeEach
    void prepare() {
        sqsAdminService = new SqsAdminService(probationOffenderEventsDlq, probationOffenderEventsQueue,
                PROBATION_OFFENDER_EVENT_SQS_DLQ_URL, PROBATION_OFFENDER_EVENT_SQS_URL);
    }

    @Test
    void shouldReplayMessagesFromDlq() {
        when(probationOffenderEventsDlq.getQueueAttributes(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL, of("ApproximateNumberOfMessages")))
                .thenReturn(new GetQueueAttributesResult().addAttributesEntry("ApproximateNumberOfMessages", "2"));
        String messageBody = "body one";
        when(probationOffenderEventsDlq.receiveMessage(new ReceiveMessageRequest(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL).withMaxNumberOfMessages(1)))
                .thenReturn(new ReceiveMessageResult().withMessages(new Message().withBody(messageBody).withReceiptHandle("receipt-handle")));
        sqsAdminService.retryProbationOffenderEventsDlqMessages();
        verify(probationOffenderEventsDlq, times(2)).receiveMessage(
                new ReceiveMessageRequest(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL).withMaxNumberOfMessages(1));
        verify(probationOffenderEventsQueue, times(2)).sendMessage(PROBATION_OFFENDER_EVENT_SQS_URL, messageBody);
        verify(probationOffenderEventsDlq, times(2)).deleteMessage(
                new DeleteMessageRequest(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL, "receipt-handle"));
    }

    @Test
    void shouldSkipReplayMessagesFromDlqWhenNoMessages() {
        when(probationOffenderEventsDlq.getQueueAttributes(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL, of("ApproximateNumberOfMessages")))
                .thenReturn(new GetQueueAttributesResult().addAttributesEntry("ApproximateNumberOfMessages", "0"));
        sqsAdminService.retryProbationOffenderEventsDlqMessages();
        verify(probationOffenderEventsDlq, times(0)).receiveMessage(
                new ReceiveMessageRequest(PROBATION_OFFENDER_EVENT_SQS_DLQ_URL).withMaxNumberOfMessages(1));
        verifyNoInteractions(probationOffenderEventsQueue);
    }
}