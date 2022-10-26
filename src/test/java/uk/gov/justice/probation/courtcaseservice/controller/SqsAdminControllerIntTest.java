package uk.gov.justice.probation.courtcaseservice.controller;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local", "sqsadmin", "unsecured"})
@DirtiesContext
public class SqsAdminControllerIntTest {

    @LocalServerPort
    protected int port;

    @Autowired
    @Qualifier("probationOffenderEventsQueue")
    private AmazonSQSAsync probationOffenderEventsQueue;

    @Autowired
    @Qualifier("probationOffenderEventsDlq")
    private AmazonSQSAsync probationOffenderEventsDlq;

    @Value("${hmpps_sqs_queues_picprobationoffendereventsqueue_queue_endpoint_url}")
    private String probationOffenderEventsQueueUrl;

    @Value("${hmpps_sqs_queues_picprobationoffendereventsqueue_dlq_endpoint_url}")
    private String probationOffenderEventsDlqUrl;

    @Test
    void givenThereAreMessagesOnDlq_whenRetryAllDlqInvoked_shouldReplayMessages() {

        purgeQueues();

        sendMessageToDlq("message body 1");
        sendMessageToDlq("message body 2");

        String sqsAdminUrl = String.format("http://localhost:%d/queue-admin/retry-probationOffenderEvents-dlq", port);
        final var response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .put(sqsAdminUrl)
                .then()
                .statusCode(200);

        var messageResult = probationOffenderEventsQueue.receiveMessage(new ReceiveMessageRequest(probationOffenderEventsQueueUrl).withMaxNumberOfMessages(2));
        List<Message> messages = messageResult.getMessages();
        assertThat(messages.size()).isEqualTo(2);
        assertThat(messages).extracting(Message::getBody).containsExactlyInAnyOrder("message body 1", "message body 2");

        var dlqMessageResult = probationOffenderEventsDlq.receiveMessage(new ReceiveMessageRequest(probationOffenderEventsDlqUrl).withMaxNumberOfMessages(2));
        assertThat(dlqMessageResult.getMessages().size()).isEqualTo(0);

        purgeQueues();
    }

    private void purgeQueues() {
        probationOffenderEventsQueue.purgeQueue(new PurgeQueueRequest(probationOffenderEventsQueueUrl));
        probationOffenderEventsDlq.purgeQueue(new PurgeQueueRequest(probationOffenderEventsDlqUrl));
    }

    private SendMessageResult sendMessageToDlq(String messageBody) {
        return probationOffenderEventsDlq.sendMessage(new SendMessageRequest().withQueueUrl(probationOffenderEventsDlqUrl).withMessageBody(messageBody));
    }
}


