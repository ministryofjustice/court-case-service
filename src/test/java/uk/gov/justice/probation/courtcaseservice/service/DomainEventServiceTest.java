package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.sqs.HmppsTopic;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventType;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReference;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.CharMatcher.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@ExtendWith(MockitoExtension.class)
public class DomainEventServiceTest {

    @InjectMocks
    private DomainEventService domainEventService;

    private HmppsTopic hmppsTopic;

    @InjectMocks
    private AmazonSNSClient snsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<PublishRequest> publishRequestArgumentCaptor;

    @BeforeEach
    public  void beforeClass() {
        hmppsTopic = new HmppsTopic("id", "arn", snsClient);
    }


    @Test
    @Ignore
    public void shouldEmit_SentencedEvent_ForEachDefendant() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn
                        (String.valueOf(buildDomainEventMessage("crn1", "cro1", "pnc1")))
                .thenReturn(String.valueOf(buildDomainEventMessage("crn2", "cro2", "pnc2")));
        when(hmppsTopic.getArn()).thenReturn("arn");

        var sentencedEventMessageAttribute = new MessageAttributeValue().withDataType("String").withStringValue(DomainEventType.SENTENCED_EVENT_TYPE.getEventTypeName());


        var hearingEntity = buildHearingEntity();

        domainEventService.emitSentencedEvent(hearingEntity);

        verify(domainEventService).emitSentencedEvent(hearingEntity);
        verify(hmppsTopic.getSnsClient(), times(2)).publish(publishRequestArgumentCaptor.capture());

        var expectedPublishRequest1 = new PublishRequest("arn", String.valueOf(buildDomainEventMessage("crn1", "cro1", "pnc1")))
                .withMessageAttributes(Collections.singletonMap("eventType", sentencedEventMessageAttribute));

        var expectedPublishRequest2 = new PublishRequest("arn", String.valueOf(buildDomainEventMessage("crn2", "cro2", "pnc2")))
                .withMessageAttributes(Collections.singletonMap("eventType", sentencedEventMessageAttribute));

        var actualPublishedRequest1 = publishRequestArgumentCaptor.getAllValues().get(0);
        var actualPublishedRequest2 = publishRequestArgumentCaptor.getAllValues().get(1);

        assertThat(actualPublishedRequest1)
                .usingRecursiveComparison()
                .ignoringFields("occurredAt")
                .isEqualTo(expectedPublishRequest1);

        assertThat(actualPublishedRequest2)
                .usingRecursiveComparison()
                .ignoringFields("occurredAt")
                .isEqualTo(expectedPublishRequest2);
    }

    private HearingEntity buildHearingEntity() {

        var defendantEntity1 = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .crn("crn1")
                        .cro("cro1")
                        .pnc("pnc1")
                        .build())
                .build();

        var defendantEntity2 = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .crn("crn2")
                        .cro("cro2")
                        .pnc("pnc2")
                        .build())
                .build();
        return HearingEntity.builder()
                .hearingId("abc123")
                .courtCase(CourtCaseEntity.builder()
                        .caseNo("case no")
                        .sourceType(COMMON_PLATFORM)
                        .build())
                .hearingDefendants(List.of(defendantEntity1, defendantEntity2))
                .build();
    }

    private List<PersonReferenceType> buildDefendantIdentifiers(String crn, String cro, String pnc) {
        return List.of(
                PersonReferenceType.builder().type("CRN").value(crn).build(),
                PersonReferenceType.builder().type("CRO").value(cro).build(),
                PersonReferenceType.builder().type("PNC").value(pnc).build()
        );
    }

    private DomainEventMessage buildDomainEventMessage(String crn, String cro, String pnc) {
        return DomainEventMessage.builder()
                .eventType(DomainEventType.SENTENCED_EVENT_TYPE.getEventTypeName())
                .version(1)
                .detailUrl("http://localhost/hearing/abc123")
                .occurredAt(LocalDateTime.now().toString())
                .personReference(PersonReference.builder()
                        .identifiers(buildDefendantIdentifiers(crn, cro, pnc))
                        .build())
                .build();
    }


}
