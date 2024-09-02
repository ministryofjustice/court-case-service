package uk.gov.justice.probation.courtcaseservice.service.listeners

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.EventFeatureSwitch
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.PersonReference
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.SQSMessage
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OffenderDomainEventListenerTest {

    private lateinit var offenderDomainEventListener: OffenderDomainEventListener;

    @Mock
    lateinit var context: ApplicationContext;

    @Mock
    lateinit var  objectMapper: ObjectMapper;

    @Mock
    lateinit var  eventFeatureSwitch: EventFeatureSwitch;

    @BeforeEach
    fun setup() {
        offenderDomainEventListener = OffenderDomainEventListener(context, objectMapper, eventFeatureSwitch)
    }

    @Test
    fun `given message older than threshold date, should not process event`(){
        val dateTime = LocalDateTime.now().minusDays(3).toString()
        val rawMessage = "{'Type': 'Notification', 'MessageId': 'ea7e4ab7-f871-4bf3-975b-13047669aaa1', 'TopicArn': 'arn:aws:sns:eu-west-2:000000000000:f6cc5ad5-4075-492f-9777-4efb498a7a70', 'Message': ''{\'eventType\':\'probation-case.engagement.created\',\'detailUrl\':\'https://domain-events-and-delius-dev.hmpps.service.justice.gov.uk/probation-case.engagement.created/XXX1234\',\'personReference\':{\'identifiers\':[{\'type\':\'CRN\',\'value\':\'XXX1234\'}]}}', 'Timestamp': ${dateTime}, 'UnsubscribeURL': 'http://localhost.localstack.cloud:4566/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:000000000000:f6cc5ad5-4075-492f-9777-4efb498a7a70:a8fbd5dd-31e2-41bb-a18b-301d1f705e5f', 'MessageAttributes': {'eventType': {'Type': 'String', 'Value': 'probation-case.engagement.created'}}, 'SignatureVersion': '1', 'Signature': 'gXhgKFwDmntbOCE9Hv61exMsB1MM1y51OrsuG8DibcL6NIo6bcHvHPTxltQgPtUVRb1xkelRGiIgAGY8L5HedGcMd8lZolX8qE5tT/1ls4KCmH2aeKjkUvoYKi8cGyvu/EQ9acG+uNw6NZRvj4w/YVX/9AqduLUfh49CYIVvT9AAuF2+aB20nKSEWrkHMQow9b1aCFs/6933q1rS2FR7VMQi4wX43jmhYqihRvDlQeCOn6mheJ5luGcXBH43svp8Rfc6DeFam2T9zY2KF7vGQxmT+oAcCEbddnqN1ytyT3OO4a2oyMXXPwnFGvoI/DGwN8gpC9K6OLsXaWT+k9pJ7Q==', 'SigningCertURL': 'http://localhost.localstack.cloud:4566/_aws/sns/SimpleNotificationService-6c6f63616c737461636b69736e696365.pem'}"
        val sqsMessage = SQSMessage("Notification","", "ea7e4ab7-f871-4bf3-975b-13047669aaa1", LocalDateTime.parse(dateTime))
        val domainEvent = DomainEvent(sqsMessage.type, "", PersonReference())
        given(objectMapper.readValue(any<String>(), any<TypeReference<SQSMessage>>())).willReturn(sqsMessage)

        offenderDomainEventListener.onDomainEvent(rawMessage)

        verify(eventFeatureSwitch, never()).isEnabled(domainEvent.eventType)
    }

    @Test
    fun `given message within threshold date, should process event`(){
        val dateTime = LocalDateTime.now().minusDays(1).minusHours(23).minusMinutes(59).minusSeconds(59).toString()
        val rawMessage = "{'Type': 'Notification', 'MessageId': 'ea7e4ab7-f871-4bf3-975b-13047669aaa1', 'TopicArn': 'arn:aws:sns:eu-west-2:000000000000:f6cc5ad5-4075-492f-9777-4efb498a7a70', 'Message': ''{\'eventType\':\'probation-case.engagement.created\',\'detailUrl\':\'https://domain-events-and-delius-dev.hmpps.service.justice.gov.uk/probation-case.engagement.created/XXX1234\',\'personReference\':{\'identifiers\':[{\'type\':\'CRN\',\'value\':\'XXX1234\'}]}}', 'Timestamp': ${dateTime}, 'UnsubscribeURL': 'http://localhost.localstack.cloud:4566/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:000000000000:f6cc5ad5-4075-492f-9777-4efb498a7a70:a8fbd5dd-31e2-41bb-a18b-301d1f705e5f', 'MessageAttributes': {'eventType': {'Type': 'String', 'Value': 'probation-case.engagement.created'}}, 'SignatureVersion': '1', 'Signature': 'gXhgKFwDmntbOCE9Hv61exMsB1MM1y51OrsuG8DibcL6NIo6bcHvHPTxltQgPtUVRb1xkelRGiIgAGY8L5HedGcMd8lZolX8qE5tT/1ls4KCmH2aeKjkUvoYKi8cGyvu/EQ9acG+uNw6NZRvj4w/YVX/9AqduLUfh49CYIVvT9AAuF2+aB20nKSEWrkHMQow9b1aCFs/6933q1rS2FR7VMQi4wX43jmhYqihRvDlQeCOn6mheJ5luGcXBH43svp8Rfc6DeFam2T9zY2KF7vGQxmT+oAcCEbddnqN1ytyT3OO4a2oyMXXPwnFGvoI/DGwN8gpC9K6OLsXaWT+k9pJ7Q==', 'SigningCertURL': 'http://localhost.localstack.cloud:4566/_aws/sns/SimpleNotificationService-6c6f63616c737461636b69736e696365.pem'}"
        val sqsMessage = SQSMessage("Notification","", "ea7e4ab7-f871-4bf3-975b-13047669aaa1", LocalDateTime.parse(dateTime))
        val domainEvent = DomainEvent(sqsMessage.type, "", PersonReference())
        given(objectMapper.readValue(any<String>(), any<TypeReference<SQSMessage>>())).willReturn(sqsMessage)
        given(objectMapper.readValue(eq(sqsMessage.message), any<TypeReference<DomainEvent>>())).willReturn(domainEvent)

        offenderDomainEventListener.onDomainEvent(rawMessage)

        verify(eventFeatureSwitch).isEnabled(domainEvent.eventType)
    }
}