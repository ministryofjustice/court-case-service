package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

import static java.util.List.of;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class SqsAdminService {
    private static final String APPROXIMATE_NUMBER_OF_MESSAGES_ATTR_NAME = "ApproximateNumberOfMessages";

    private final AmazonSQSAsync probationOffenderEventsDlq;

    private final AmazonSQSAsync probationOffenderEventsQueue;

    private final String probationOffenderEventsDlqUrl;

    private final String probationOffenderEventsQueueUrl;


    public SqsAdminService(@Qualifier("probationOffenderEventsDlq") AmazonSQSAsync probationOffenderEventsDlq,
                           @Qualifier("probationOffenderEventsQueue") AmazonSQSAsync probationOffenderEventsQueue,
                           @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.dlq_endpoint_url}") String probationOffenderEventsDlqUrl,
                           @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.queue_endpoint_url}") String probationOffenderEventsQueueUrl
    ) {
        this.probationOffenderEventsDlq = probationOffenderEventsDlq;
        this.probationOffenderEventsQueue = probationOffenderEventsQueue;
        this.probationOffenderEventsDlqUrl = probationOffenderEventsDlqUrl;
        this.probationOffenderEventsQueueUrl = probationOffenderEventsQueueUrl;
    }

    public void retryProbationOffenderEventsDlqMessages() {

        log.info("Replaying probation offender event dlq messages.");

        var dlqAttributes = probationOffenderEventsDlq.getQueueAttributes(probationOffenderEventsDlqUrl,
                of(APPROXIMATE_NUMBER_OF_MESSAGES_ATTR_NAME));
        var messageCount = ofNullable(dlqAttributes.getAttributes())
                .map(attrMap -> attrMap.get(APPROXIMATE_NUMBER_OF_MESSAGES_ATTR_NAME))
                .map(Integer::parseInt)
                .orElse(0);

        log.info("Found {} messages on the dlq", messageCount);

        IntStream.range(0, messageCount).forEach(value -> {
            var dlqMessages = probationOffenderEventsDlq.receiveMessage(
                            new ReceiveMessageRequest(probationOffenderEventsDlqUrl).withMaxNumberOfMessages(1))
                    .getMessages();
            dlqMessages.forEach(message -> {
                probationOffenderEventsQueue.sendMessage(probationOffenderEventsQueueUrl, message.getBody());
                probationOffenderEventsDlq.deleteMessage(new DeleteMessageRequest(probationOffenderEventsDlqUrl, message.getReceiptHandle()));
            });
        });

        log.info("Replayed {} messages.", messageCount);
    }
}
