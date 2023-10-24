package uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent

interface IEventNotifier {
  fun process(domainEvent: DomainEvent)
}

abstract class EventNotifier(
) : IEventNotifier {

  companion object {
    val LOG: Logger = LoggerFactory.getLogger(this::class.java)
  }

  final override fun process(domainEvent: DomainEvent) {
    LOG.debug("Entered process for ${this::class.java.name} type: ${domainEvent.eventType}")
    this.processEvent(domainEvent)
  }

  abstract fun processEvent(domainEvent: DomainEvent)
}
