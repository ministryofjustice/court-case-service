package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
@AllArgsConstructor
public class DomainEventService {

    private final HmppsQueueService hmppsQueueService;
    private final ObjectMapper objectMapper;
    private final String EVENT_TYPE = "eventType";

    private HmppsTopic getDomainEventTopic() {
        return hmppsQueueService.findByTopicId("hmpps-domain-events");
    }

    void emitSentencedEvent() throws JsonProcessingException {
        //TODO add required parameters.

        var hmppsTopic = getDomainEventTopic();

        var sentencedEventMessage = DomainEventMessage.builder()
                .eventType(DomainEventType.SENTENCED_EVENT_TYPE.getEventTypeName())
                .version(1)
                .detailUrl(generateDetailUrl("path", "host", "param")) //TODO
                .occurredAt(LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();

        var sentencedEventMessageAttribute = new MessageAttributeValue().withDataType("String").withStringValue(sentencedEventMessage.getEventType());

        var publishRequest = new PublishRequest(hmppsTopic.getArn(), objectMapper.writeValueAsString(sentencedEventMessage))
                .withMessageAttributes(Collections.singletonMap(EVENT_TYPE, sentencedEventMessageAttribute));

        hmppsTopic.getSnsClient().publish(publishRequest);
    }

    private String generateDetailUrl(String path, String host, String parameter) { //TODO
        return UriComponentsBuilder.newInstance().scheme("https").host(host).path(path).buildAndExpand(parameter).toUriString();
    }
}
