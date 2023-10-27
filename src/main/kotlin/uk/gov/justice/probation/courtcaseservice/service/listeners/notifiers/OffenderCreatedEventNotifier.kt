package uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers

import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.client.OffenderDetailRestClient
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.service.ProbationCaseEngagementService

const val NEW_OFFENDER_CREATED = "probation-case.engagement.created"

@Component(value = NEW_OFFENDER_CREATED)
class OffenderCreatedEventNotifier(
    val offenderDetailRestClient: OffenderDetailRestClient,
    val probationCaseEngagementService: ProbationCaseEngagementService
) : EventNotifier() {
    override fun processEvent(domainEvent: DomainEvent) {
        val offenderDetailUrl = domainEvent.detailUrl
        val crn = domainEvent.personReference?.identifiers?.first{it.type == "CRN"}
        LOG.debug("Enter processEvent with  Info:$offenderDetailUrl")
        offenderDetailRestClient.getOffenderDetail(offenderDetailUrl, crn?.value).block()
            .let { probationCaseEngagementService.updateMatchingDefendantsWithCrn(it) }
    }
}


