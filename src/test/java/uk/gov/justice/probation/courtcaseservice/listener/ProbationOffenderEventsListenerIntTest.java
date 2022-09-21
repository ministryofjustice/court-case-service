package uk.gov.justice.probation.courtcaseservice.listener;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.UserAgnosticOffenderService;
import uk.gov.justice.probation.courtcaseservice.testUtil.OffenderEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProbationOffenderEventsListenerIntTest extends BaseIntTest {

    @SpyBean
    UserAgnosticOffenderService offenderService;

    @SpyBean
    OffenderRepository offenderRepository;

    ObjectMapper objectMapper;
    @Captor
    ArgumentCaptor<String> crnArgumentCaptor;

    @Captor
    ArgumentCaptor<OffenderEntity> offenderEntityArgumentCaptor;


    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        getOffenderEventReceiverQueueSqsClient().purgeQueue(new PurgeQueueRequest(getOffenderEventReceiverQueueUrl()));
    }

    @Test
    public void shouldProcess_OffenderEventChangedMessage_AndUpdateOffenderProbationStatus() throws JsonProcessingException, InterruptedException {
        var offenderEntity = OffenderEntity.builder()
                .id(Long.valueOf(-1000001))
                .cro("CROINT007")
                .crn("X320741")
                .pnc("PNCINT007")
                .probationStatus(OffenderProbationStatus.CURRENT)
                .previouslyKnownTerminationDate(LocalDate.of(2010, 1, 1))
                .awaitingPsr(true)
                .breach(true)
                .preSentenceActivity(true)
                .build();

        when(offenderRepository.findByCrn("X320741")).thenReturn(Optional.ofNullable(offenderEntity));

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

        Thread.sleep(2000);
       // assertOffenderEventReceiverQueueHasProcessedMessages();

        verify(offenderService).updateOffenderProbationStatus(crnArgumentCaptor.capture());
        assertThat(crnArgumentCaptor.getValue()).isEqualTo("X320741");

        verify(offenderRepository).findByCrn(crnArgumentCaptor.capture());

        verify(offenderRepository).save(offenderEntityArgumentCaptor.capture());

        var offenderEntityToUpdate = offenderEntityArgumentCaptor.getValue();
        assertThat(offenderEntityToUpdate.getCrn()).isEqualTo("X320741");
        assertThat(offenderEntityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(offenderEntityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2010, 4, 5));
        assertThat(offenderEntityToUpdate.isBreach()).isEqualTo(true);
        assertThat(offenderEntityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(offenderEntityToUpdate.getAwaitingPsr()).isEqualTo(false);
    }

    private PublishResult publishOffenderEvent(OffenderEvent offenderEvent) throws JsonProcessingException {

        var messageAttribute = new MessageAttributeValue().withDataType("String").withStringValue("SENTENCE_CHANGED");
        var eventJson = objectMapper.writeValueAsString(offenderEvent);
        var offenderEventRequest = new PublishRequest(getOffenderEventTopic().getArn(), eventJson)
                .withMessageAttributes(Collections.singletonMap("eventType", messageAttribute));

        return getOffenderEventTopic().getSnsClient().publish(offenderEventRequest);

    }
}
