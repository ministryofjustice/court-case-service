package uk.gov.justice.probation.courtcaseservice.service.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers.IEventNotifier
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.SQSMessage
import java.util.concurrent.CompletableFuture

const val PIC_NEW_OFFENDER_EVENT_QUEUE_CONFIG_KEY = "picnewoffendereventsqueue"

@Service
class OffenderDomainEventListenerService(
  val context: ApplicationContext,
  val objectMapper: ObjectMapper,
) {

  private companion object {
    val LOG: Logger = LoggerFactory.getLogger(this::class.java)
  }

  //TODO use @SqsListener after spring upgrade.
 // @SqsListener(PIC_NEW_OFFENDER_EVENT_QUEUE_CONFIG_KEY, factory = "hmppsQueueContainerFactoryProxy")
  @JmsListener(destination = PIC_NEW_OFFENDER_EVENT_QUEUE_CONFIG_KEY, containerFactory =  "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(
    rawMessage: String,
  ): CompletableFuture<Void> {
    return asCompletableFuture {
        try {
          LOG.debug("Enter onDomainEvent")
          val sqsMessage: SQSMessage = objectMapper.readValue(rawMessage)
          LOG.debug("Received message: type:${sqsMessage.type} message:${sqsMessage.message}")

          when (sqsMessage.type) {
            "Notification" -> {
              val domainEvent = objectMapper.readValue<DomainEvent>(sqsMessage.message)
              getNotifier(domainEvent)?.process(domainEvent)
            }
            else -> LOG.info("Received a message I wasn't expecting Type: ${sqsMessage.type}")
          }
        } catch (e: Exception) {
          LOG.error("Failed to process domain event", e)
          throw e
        }

    }
  }

  fun getNotifier(domainEvent: DomainEvent): IEventNotifier? {
    if (context.containsBean(domainEvent.eventType)) {
      return context.getBean(domainEvent.eventType) as IEventNotifier
    }
    LOG.info("EventNotifier does not exist for Type:'${domainEvent.eventType}'")
    return null
  }
}

private fun asCompletableFuture(
  process: suspend () -> Unit,
): CompletableFuture<Void> {
  return CoroutineScope(Dispatchers.Default).future {
    process()
  }.thenAccept { }
}
