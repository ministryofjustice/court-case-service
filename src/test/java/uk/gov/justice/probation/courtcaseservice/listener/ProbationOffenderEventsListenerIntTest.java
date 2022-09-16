package uk.gov.justice.probation.courtcaseservice.listener;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.testUtil.OffenderEvent;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class ProbationOffenderEventsListenerIntTest extends BaseIntTest {

    @SpyBean
    OffenderService offenderService;

    ObjectMapper objectMapper;

    ArgumentCaptor<String> crnArgumentCaptor;

    @BeforeEach
    public void setUp(){
        objectMapper = new ObjectMapper();
        getOffenderEventReceiverQueueSqsClient().purgeQueue(new PurgeQueueRequest(getOffenderEventReceiverQueueUrl()));
    }

    @Test
    public void shouldProcess_OffenderEventChangedMessage_AndUpdateOffenderProbationStatus() throws JsonProcessingException {
        OffenderEvent sentencedEvent = OffenderEvent.builder()
                .crn("X320741")
                .offenderId(1L)
                .nomsNumber("n123")
                .sourceId(2L)
                .eventDateTime(LocalDateTime.now())
                .build();

        var result = publishOffenderEvent(sentencedEvent);

        assertThat(result.getSdkHttpMetadata().getHttpStatusCode()).isEqualTo(200);
        assertThat(result.getMessageId()).isNotNull();

        verify(offenderService).updateOffenderProbationStatus(crnArgumentCaptor.capture());
        assertThat(crnArgumentCaptor.getValue()).isEqualTo("X320741");
    }

    private PublishResult publishOffenderEvent(OffenderEvent offenderEvent) throws JsonProcessingException {

        var messageAttribute = new MessageAttributeValue().withDataType("String").withStringValue("SENTENCE_CHANGED");
        var eventJson = objectMapper.writeValueAsString(offenderEvent);
        var offenderEventRequest = new PublishRequest(getOffenderEventTopic().getArn(), eventJson)
                .withMessageAttributes(Collections.singletonMap("eventType", messageAttribute));

        return getOffenderEventTopic().getSnsClient().publish(offenderEventRequest);

    }
}
