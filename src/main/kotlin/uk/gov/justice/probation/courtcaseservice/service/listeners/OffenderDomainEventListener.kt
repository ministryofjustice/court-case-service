package uk.gov.justice.probation.courtcaseservice.service.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.EventFeatureSwitch
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.SQSMessage
import uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers.IEventNotifier

const val PIC_NEW_OFFENDER_EVENT_QUEUE_CONFIG_KEY = "picnewoffendereventsqueue"

@Component
class OffenderDomainEventListener(
    val context: ApplicationContext,
    val objectMapper: ObjectMapper,
    val eventFeatureSwitch: EventFeatureSwitch,
) {
    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @SqsListener(PIC_NEW_OFFENDER_EVENT_QUEUE_CONFIG_KEY, factory = "hmppsQueueContainerFactoryProxy")
    fun onDomainEvent(
        rawMessage: String,
    ) {
        var dLQException: Exception? = null
        try {
            LOG.debug("Enter onDomainEvent")
            val sqsMessage = objectMapper.readValue<SQSMessage>(rawMessage)
            LOG.debug("Received message: type:${sqsMessage.type} message:${sqsMessage.message}")
            when (sqsMessage.type) {
                "Notification" -> {
                    val domainEvent = objectMapper.readValue<DomainEvent>(sqsMessage.message)
                    val enabled = eventFeatureSwitch.isEnabled(domainEvent.eventType)
                    if (enabled) {
                        try {
                            getEventNotifier(domainEvent)?.process(domainEvent)
                        } catch (e: Exception) {
                            LOG.error("Failed to process know domain event type:${domainEvent.eventType}", e)
                            dLQException = e
                        }
                    } else {
                        LOG.info("Received a message I wasn't expecting Type: ${sqsMessage.type}")
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Failed to process domain event", e)
            throw e
        }
        if (dLQException != null) {
            // Throw exception caught in processing known events to push message back on event queue
            throw dLQException
        }
    }

    fun getEventNotifier(domainEvent: DomainEvent): IEventNotifier? {
        if (context.containsBean(domainEvent.eventType)) {
            return context.getBean(domainEvent.eventType) as IEventNotifier
        }
        LOG.info("EventNotifier does not exist for Type:'${domainEvent.eventType}'")
        return null
    }
}
