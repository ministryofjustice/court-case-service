package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.testUtil.OffenderEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class ProbationOffenderEventsListenerIntTest extends BaseIntTest {

    @Autowired
    OffenderRepository offenderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        getOffenderEventReceiverQueueSqsClient().purgeQueue(
                PurgeQueueRequest.builder()
                        .queueUrl(getOffenderEventReceiverQueueUrl())
                        .build());

    }

    @Test
    public void shouldProcess_OffenderEventChangedMessage_AndUpdateOffenderProbationStatus() throws JsonProcessingException, ExecutionException, InterruptedException {
        String crnForTest = "X781345";

        var offenderEntityBeforeUpdate = offenderRepository.findByCrn(crnForTest).get();

        assertThat(offenderEntityBeforeUpdate.getCrn()).isEqualTo(crnForTest);
        assertThat(offenderEntityBeforeUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(offenderEntityBeforeUpdate.isBreach()).isEqualTo(true);
        assertThat(offenderEntityBeforeUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(offenderEntityBeforeUpdate.getAwaitingPsr()).isEqualTo(true);


        OffenderEvent sentencedEvent = OffenderEvent.builder()
            .crn(crnForTest)
            .offenderId(1L)
            .nomsNumber("n123")
            .sourceId(2L)
            .eventDateTime(LocalDateTime.now())
            .build();

        var publishResponse = publishOffenderProbationStatusChangeEvent(sentencedEvent).get();

        assertThat(publishResponse.sdkHttpResponse().isSuccessful()).isTrue();
        assertThat(publishResponse.messageId()).isNotNull();

        assertOffenderEventReceiverQueueHasProcessedMessages();

        var updatedOffenderEntity = offenderRepository.findByCrn(crnForTest).get();

        assertThat(updatedOffenderEntity.getCrn()).isEqualTo(crnForTest);
        assertThat(updatedOffenderEntity.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(updatedOffenderEntity.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2010, 4, 5));
        assertThat(updatedOffenderEntity.isBreach()).isEqualTo(true);
        assertThat(updatedOffenderEntity.isPreSentenceActivity()).isEqualTo(true);
        assertThat(updatedOffenderEntity.getAwaitingPsr()).isEqualTo(false);
    }

    private CompletableFuture<PublishResponse> publishOffenderProbationStatusChangeEvent(OffenderEvent offenderEvent) throws JsonProcessingException {

        var messageAttribute = MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("SENTENCE_CHANGED")
                .build();
        var eventJson = objectMapper.writeValueAsString(offenderEvent);
        var offenderEventRequest = PublishRequest.builder()
                .topicArn(getOffenderEventTopic().getArn())
                .message(eventJson)
                .messageAttributes(Collections.singletonMap("eventType", messageAttribute))
                .build();

        return getOffenderEventTopic().getSnsClient().publish(offenderEventRequest);
    }
}
