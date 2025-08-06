package uk.gov.justice.probation.courtcaseservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@ExtendWith(MockitoExtension.class)
public class DomainEventServiceTest {

    private DomainEventService domainEventService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SnsAsyncClient snsClient;

    @Captor
    private ArgumentCaptor<PublishRequest> publishRequestArgumentCaptor;

    @BeforeEach
    public void beforeClass() {
        HmppsTopic hmppsTopic = new HmppsTopic("id", "arn", snsClient);
        domainEventService = new DomainEventService(objectMapper, hmppsTopic);
    }


    @Test
    public void shouldEmit_SentencedEvent_ForEachDefendant() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn
                        (String.valueOf(buildDomainEventMessage("crn1", "cro1", "pnc1","personId1")))
                .thenReturn(String.valueOf(buildDomainEventMessage("crn2", "cro2", "pnc2","personId2")));

        var hearingEntity = buildHearingEntity();

        CompletableFuture<PublishResponse> result = CompletableFuture.completedFuture( PublishResponse.builder().messageId("messageId").build() );

        when(snsClient.publish(ArgumentMatchers.any(PublishRequest.class))).thenReturn(result);

        domainEventService.emitSentencedEvent(hearingEntity);

        verify(snsClient, times(2)).publish(publishRequestArgumentCaptor.capture());

        var actualPublishedRequest1 = publishRequestArgumentCaptor.getAllValues().get(0);
        var actualPublishedRequest2 = publishRequestArgumentCaptor.getAllValues().get(1);

        assertThat(actualPublishedRequest1.messageAttributes().get("eventType").dataType()).isEqualTo("String");
        assertThat(actualPublishedRequest1.messageAttributes().get("eventType").stringValue()).isEqualTo("court.case.sentenced");

        assertThat(actualPublishedRequest1.message()).contains("crn1");
        assertThat(actualPublishedRequest1.message()).contains("cro1");
        assertThat(actualPublishedRequest1.message()).contains("pnc1");
        assertThat(actualPublishedRequest1.message()).contains("personId1");


        assertThat(actualPublishedRequest2.messageAttributes().get("eventType").dataType()).isEqualTo("String");
        assertThat(actualPublishedRequest2.messageAttributes().get("eventType").stringValue()).isEqualTo("court.case.sentenced");

        assertThat(actualPublishedRequest2.message()).contains("crn2");
        assertThat(actualPublishedRequest2.message()).contains("cro2");
        assertThat(actualPublishedRequest2.message()).contains("pnc2");
        assertThat(actualPublishedRequest2.message()).contains("personId2");
    }

    @Test
    public void shouldEmit_NDeliusRecord_Linked_ForDefendant() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn(String.valueOf(buildNDeliusDomainEventMessage("crn1",  "defendantId")));
        var defendantEntity = buildDefendantEntity();
        CompletableFuture<PublishResponse> result = CompletableFuture.completedFuture( PublishResponse.builder().messageId("messageId").build() );
        when(snsClient.publish(ArgumentMatchers.any(PublishRequest.class))).thenReturn(result);

        domainEventService.emitLinkNDeliusRecordEvent(defendantEntity);

        verify(snsClient, times(1)).publish(publishRequestArgumentCaptor.capture());
        var actualPublishedRequest = publishRequestArgumentCaptor.getAllValues().getFirst();
        assertThat(actualPublishedRequest.messageAttributes().get("eventType").dataType()).isEqualTo("String");
        assertThat(actualPublishedRequest.messageAttributes().get("eventType").stringValue()).isEqualTo("ndelius.record.linked");
        assertThat(actualPublishedRequest.message()).contains("crn1");
        assertThat(actualPublishedRequest.message()).contains("defendantId");
    }

    @Test
    public void shouldEmit_NDeliusRecord_UnLinked_ForDefendant() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn(String.valueOf(buildNDeliusDomainEventMessage("crn1",  "defendantId")));
        var defendantEntity = buildDefendantEntity();
        CompletableFuture<PublishResponse> result = CompletableFuture.completedFuture( PublishResponse.builder().messageId("messageId").build() );
        when(snsClient.publish(ArgumentMatchers.any(PublishRequest.class))).thenReturn(result);

        domainEventService.emitUnLinkNDeliusRecordEvent(defendantEntity);

        verify(snsClient, times(1)).publish(publishRequestArgumentCaptor.capture());
        var actualPublishedRequest = publishRequestArgumentCaptor.getAllValues().getFirst();
        assertThat(actualPublishedRequest.messageAttributes().get("eventType").dataType()).isEqualTo("String");
        assertThat(actualPublishedRequest.messageAttributes().get("eventType").stringValue()).isEqualTo("ndelius.record.unlinked");
        assertThat(actualPublishedRequest.message()).contains("crn1");
        assertThat(actualPublishedRequest.message()).contains("defendantId");
    }

    private DefendantEntity buildDefendantEntity() {
        return DefendantEntity.builder()
            .crn("crn1")
            .personId("defendantId").build();
    }
    
    private HearingEntity buildHearingEntity() {

        var defendantEntity1 = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .crn("crn1")
                        .cro("cro1")
                        .pnc("pnc1")
                        .personId("personId1")
                        .build())
                .build();

        var defendantEntity2 = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .crn("crn2")
                        .cro("cro2")
                        .pnc("pnc2")
                        .personId("personId2")
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

    private List<PersonReferenceType> buildDefendantIdentifiers(String crn, String cro, String pnc,String personId) {
        return List.of(
                PersonReferenceType.builder().type("CRN").value(crn).build(),
                PersonReferenceType.builder().type("CRO").value(cro).build(),
                PersonReferenceType.builder().type("PNC").value(pnc).build(),
                PersonReferenceType.builder().type("PERSON_ID").value(personId).build()
        );
    }

    private DomainEventMessage buildDomainEventMessage(String crn, String cro, String pnc,String personId) {
        return DomainEventMessage.builder()
                .eventType(DomainEventType.SENTENCED_EVENT_TYPE.getEventTypeName())
                .version(1)
                .detailUrl("http://localhost/hearing/abc123")
                .occurredAt(LocalDateTime.now().toString())
                .personReference(PersonReference.builder()
                        .identifiers(buildDefendantIdentifiers(crn, cro, pnc,personId))
                        .build())
                .build();
    }

    private DomainEventMessage buildNDeliusDomainEventMessage(String crn, String defendantId) {
        return DomainEventMessage.builder()
            .eventType(DomainEventType.NDELIUS_RECORD_LINKED_EVENT_TYPE.getEventTypeName())
            .version(1)
            .occurredAt(LocalDateTime.now().toString())
            .personReference(PersonReference.builder()
                .identifiers(buildBasicDefendantIdentifiers(crn, defendantId))
                .build())
            .build();
    }

    private List<PersonReferenceType> buildBasicDefendantIdentifiers(String crn, String defendantId) {
        return List.of(
            PersonReferenceType.builder().type("CRN").value(crn).build(),
            PersonReferenceType.builder().type("DEFENDANT_ID").value(defendantId).build()
        );
    }

}
