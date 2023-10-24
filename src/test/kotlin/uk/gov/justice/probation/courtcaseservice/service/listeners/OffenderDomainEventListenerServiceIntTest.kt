package uk.gov.justice.probation.courtcaseservice.service.listeners

import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.SQSMessage
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers.NEW_OFFENDER_CREATED
import java.util.*

class OffenderDomainEventListenerServiceIntTest : BaseIntTest() {

    lateinit var defendantRepository: DefendantRepository

    @Autowired
    lateinit var domainEventListenerService: OffenderDomainEventListenerService

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        newOffenderEventReceiverQueueSqsClient.purgeQueue(PurgeQueueRequest(newOffenderEventReceiverQueueQueueUrl))
    }

    @Test
    fun `Test new offender created event processed successfully`() {
        // Given
        val domainEvent = createDomainEventJson(NEW_OFFENDER_CREATED, "X1234")
        val jsonSqsMessage = createSQSMessage(domainEvent)

        // When
        val result = publishNewOffenderCreatedEvent(jsonSqsMessage)

        //Then
        Assertions.assertThat(result.sdkHttpMetadata.httpStatusCode).isEqualTo(200)
        Assertions.assertThat(result.messageId).isNotNull()

        assertNewOffenderDomainEventReceiverQueueHasProcessedMessages()
        //TODO assert defendant update.
    }

    @Throws(JsonProcessingException::class)
    fun publishNewOffenderCreatedEvent(eventJson: String): PublishResult {
        val offenderEventRequest = PublishRequest(domainEventsTopicArn, eventJson)
        return domainEventsTopic.snsClient.publish(offenderEventRequest)
    }


    fun createDomainEvent(eventType: String, crn: String = "test"): DomainEvent {
        return DomainEvent(eventType = eventType, createDetailUrl(crn))
    }

    fun createSQSMessage(domainEventJson: String): String {
        val sqaMessage = SQSMessage(type = "Notification", messageId = "123", message = domainEventJson)
        return objectMapper.writeValueAsString(sqaMessage)
    }

    fun createDomainEventPublishRequest(eventType: String, domainEvent: String): PublishRequest? {
        return PublishRequest(domainEventsTopicArn, domainEvent)

    }

    fun createDomainEventJson(eventType: String, crn: String): String {
        val detailUrl = createDetailUrl(crn)
        return "{\"eventType\":\"$eventType\",\"detailUrl\":${detailUrl}}"
    }

    fun createDetailUrl(crn: String): String {
        val builder = StringBuilder()
        builder.append("https://domain-events-and-delius-dev.hmpps.service.justice.gov.uk/probation-case.engagement.created/")
        builder.append(crn)
        return builder.toString()
    }
}