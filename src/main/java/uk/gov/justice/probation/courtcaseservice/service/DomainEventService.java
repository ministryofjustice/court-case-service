package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventType;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReference;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@NoArgsConstructor
public class DomainEventService {
    private HmppsQueueService hmppsQueueService;
    private  ObjectMapper objectMapper;
    @Value("${ingress.url}")
    private  String host;
    private final String EVENT_TYPE_KEY = "eventType";

   final String HEARING_BY_HEARING_ID_TEMPLATE = "https://%s/hearing/%s";

    @Autowired
    public DomainEventService(HmppsQueueService hmppsQueueService, ObjectMapper objectMapper) {
        this.hmppsQueueService = hmppsQueueService;
        this.objectMapper = objectMapper;
    }

    private HmppsTopic getDomainEventTopic() {
        return hmppsQueueService.findByTopicId("hmpps-domain-events");
    }

    void emitSentencedEvent(HearingEntity hearingEntity) {

        var hmppsTopic = getDomainEventTopic();

        var sentencedEventType = DomainEventType.SENTENCED_EVENT_TYPE;
        var detailUrl = String.format(HEARING_BY_HEARING_ID_TEMPLATE, host, hearingEntity.getHearingId());
        var occurredAt = LocalDateTime.now();

        hearingEntity.getHearingDefendants()
                .forEach(hearingDefendantEntity -> {

                    var sentencedEventMessage = DomainEventMessage.builder()
                            .eventType(sentencedEventType.getEventTypeName())
                            .version(1)
                            .detailUrl(detailUrl)
                            .occurredAt(occurredAt.toString())
                            .personReference(PersonReference.builder()
                                    .identifiers(buildDefendantIdentifiers(hearingDefendantEntity.getDefendant()))
                                    .build())
                            .build();

                    var sentencedEventMessageAttribute = new MessageAttributeValue().withDataType("String").withStringValue(sentencedEventMessage.getEventType());

                    try {
                        PublishRequest publishRequest = new PublishRequest(hmppsTopic.getArn(), objectMapper.writeValueAsString(sentencedEventMessage))
                                .withMessageAttributes(Collections.singletonMap(EVENT_TYPE_KEY, sentencedEventMessageAttribute));

                        hmppsTopic.getSnsClient().publish(publishRequest);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to emit a sentenced event %s", e);
                    }
                });
    }

    private List<PersonReferenceType> buildDefendantIdentifiers(DefendantEntity defendant) {
        return List.of(
                PersonReferenceType.builder().type("CRN").value(defendant.getCrn()).build(),
                PersonReferenceType.builder().type("CRO").value(defendant.getCro()).build(),
                PersonReferenceType.builder().type("PNC").value(defendant.getPnc()).build()
        );
    }
}
