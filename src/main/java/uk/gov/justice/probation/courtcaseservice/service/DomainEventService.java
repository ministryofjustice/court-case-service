package uk.gov.justice.probation.courtcaseservice.service;

import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.sqs.HmppsTopic;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventType;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReference;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@NoArgsConstructor
public class DomainEventService {
    private  ObjectMapper objectMapper;
    @Value("${ingress.url}")
    private  String host;
    private final String EVENT_TYPE_KEY = "eventType";

    final String HEARING_BY_HEARING_ID_TEMPLATE = "https://%s/hearing/%s";
    private HmppsTopic topic;

    @Autowired
    public DomainEventService(ObjectMapper objectMapper, @Qualifier("hmppsDomainEventsTopic") HmppsTopic topic) {
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    void emitSentencedEvent(HearingEntity hearingEntity) {

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

                    var sentencedEventMessageAttribute = MessageAttributeValue.builder().dataType("String").stringValue(sentencedEventType.getEventTypeName()).build();

                    try {
                        PublishRequest publishRequest = PublishRequest.builder()
                                .topicArn(topic.getArn())
                                .message(objectMapper.writeValueAsString(sentencedEventMessage))
                                .messageAttributes(Collections.singletonMap(EVENT_TYPE_KEY, sentencedEventMessageAttribute))
                                .build();

                        topic.getSnsClient().publish(publishRequest);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to emit a sentenced event %s", e);
                    }
                });
    }

    void emitLinkNDeliusRecordEvent(DefendantEntity defendant) {
        var sentencedEventType = DomainEventType.NDELIUS_RECORD_LINKED_EVENT_TYPE;
        emitNDeliusRecordEvent(defendant, sentencedEventType);
    }

    void emitUnLinkNDeliusRecordEvent(DefendantEntity defendant) {
        var sentencedEventType = DomainEventType.NDELIUS_RECORD_UNLINKED_EVENT_TYPE;
        emitNDeliusRecordEvent(defendant, sentencedEventType);
    }

    private void emitNDeliusRecordEvent(DefendantEntity defendant, DomainEventType sentencedEventType) {
        var sentencedEventMessage = DomainEventMessage.builder()
            .eventType(sentencedEventType.getEventTypeName())
            .version(1)
            .occurredAt(LocalDateTime.now().toString())
            .personReference(PersonReference.builder()
                .identifiers(buildBasicDefendantIdentifiers(defendant))
                .build())
            .build();

        var sentencedEventMessageAttribute = MessageAttributeValue.builder().dataType("String").stringValue(sentencedEventType.getEventTypeName()).build();

        try {
            PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topic.getArn())
                .message(objectMapper.writeValueAsString(sentencedEventMessage))
                .messageAttributes(Collections.singletonMap(EVENT_TYPE_KEY, sentencedEventMessageAttribute))
                .build();

            topic.getSnsClient().publish(publishRequest);
        } catch (JsonProcessingException e) {
            log.error("Failed to emit a {} event", sentencedEventType);
            log.error("JsonProcessingException %s", e);
        }
    }

    private List<PersonReferenceType> buildDefendantIdentifiers(DefendantEntity defendant) {
        return List.of(
                PersonReferenceType.builder().type("CRN").value(defendant.getCrn()).build(),
                PersonReferenceType.builder().type("CRO").value(defendant.getCro()).build(),
                PersonReferenceType.builder().type("PNC").value(defendant.getPnc()).build(),
                PersonReferenceType.builder().type("PERSON_ID").value(defendant.getPersonId()).build()
        );
    }

    private List<PersonReferenceType> buildBasicDefendantIdentifiers(DefendantEntity defendant) {
        return List.of(
            uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType.builder().type("CRN").value(defendant.getCrn()).build(),
            cIdOrDefendantId(defendant)
        );
    }

    private PersonReferenceType cIdOrDefendantId(DefendantEntity defendant) {
        if(defendant.getCId() != null) {
            return uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType.builder().type("C_ID").value(defendant.getCId()).build();
        }
        return uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType.builder().type("DEFENDANT_ID").value(defendant.getDefendantId()).build();
    }
}
