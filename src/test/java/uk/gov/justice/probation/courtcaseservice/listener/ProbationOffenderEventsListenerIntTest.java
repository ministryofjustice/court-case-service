package uk.gov.justice.probation.courtcaseservice.listener;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.testUtil.OffenderEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class ProbationOffenderEventsListenerIntTest extends BaseIntTest {

    @Autowired
    OffenderRepository offenderRepository;

    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        getOffenderEventReceiverQueueSqsClient().purgeQueue(new PurgeQueueRequest(getOffenderEventReceiverQueueUrl()));
    }

    @Test
    public void shouldProcess_OffenderEventChangedMessage_AndUpdateOffenderProbationStatus() throws JsonProcessingException {

        String crnForTest = "X781345";
        OffenderEvent sentencedEvent = OffenderEvent.builder()
            .crn(crnForTest)
            .offenderId(1L)
            .nomsNumber("n123")
            .sourceId(2L)
            .eventDateTime(LocalDateTime.now())
            .build();

        var result = publishOffenderEvent(sentencedEvent);

        assertThat(result.getSdkHttpMetadata().getHttpStatusCode()).isEqualTo(200);
        assertThat(result.getMessageId()).isNotNull();

        assertOffenderEventReceiverQueueHasProcessedMessages();

        var offenderEntityToUpdate = offenderRepository.findByCrn(crnForTest).get();

        assertThat(offenderEntityToUpdate.getCrn()).isEqualTo(crnForTest);
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
