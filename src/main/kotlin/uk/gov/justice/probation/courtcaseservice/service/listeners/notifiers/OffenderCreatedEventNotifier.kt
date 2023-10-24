package uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers

import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.client.OffenderDetailRestClient
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.service.OffenderMapperService

const val NEW_OFFENDER_CREATED = "probation-case.engagement.created"

@Component(value = NEW_OFFENDER_CREATED)
class OffenderCreatedEventNotifier(
    val offenderDetailRestClient: OffenderDetailRestClient,
    val offenderMapperService: OffenderMapperService
) : EventNotifier() {
    override fun processEvent(domainEvent: DomainEvent) {
        val offenderDetailUrl = domainEvent.detailUrl
        LOG.debug("Enter processEvent with  Info:$offenderDetailUrl")
        offenderDetailRestClient.getOffenderDetail(offenderDetailUrl, null).block()
            .let { offenderMapperService.mapOffenderToMatchingDefendants(it) }
    }
}
//TODO Domain event message
/*{
  "eventType": "probation-case.engagement.created",
  "version": 1,
  "detailUrl": "https://domain-events-and-delius-dev.hmpps.service.justice.gov.uk/probation-case.engagement.created/X729438",
  "occurredAt": "2023-10-12T09:01:21.772+01:00",
  "description": "A probation case record for a person has been created in Delius",
  "personReference": {
  "identifiers": [
  {
    "type": "CRN",
    "value": "X729438"
  }
  ]
},
  "additionalInformation": {}
}*/

// TODO new Offender details
/*{
  "identifiers": {
  "crn": "string",
  "pnc": "string"
},
  "name": {
  "forename": "string",
  "surname": "string",
  "otherNames": [
  "string"
  ]
},
  "dateOfBirth": "2023-10-13"
}*/

//TODO raw sqs message
/*{
  "Type": "Notification",
  "MessageId": "045e81cd-c5b8-533d-9c7c-dc6c6fab797b",
  "TopicArn": "arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-e29fb030a51b3576dd645aa5e460e573",
  "Message": "{\"eventType\":\"probation-case.engagement.created\",\"version\":1,\"detailUrl\":\"https://domain-events-and-delius-dev.hmpps.service.justice.gov.uk/probation-case.engagement.created/X729438\",\"occurredAt\":\"2023-10-12T09:01:21.772+01:00\",\"description\":\"A probation case record for a person has been created in Delius\",\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X729438\"}]},\"additionalInformation\":{}}",
  "Timestamp": "2023-10-12T08:01:23.219Z",
  "SignatureVersion": "1",
  "Signature": "kOsqe3KfyTDP9hqQt2Mkbs25czIIHSJ8ZTr1Agfbu3eW+X5TO9P7A7Fbz5yDxIsqTW1Bul8rxQ+s8RcfnJqozkC5IrX3JPNEI7jpaUdGm5jkXEVsNTOqNzZ3b57zTrbuccXdSaCmMR+owQSHFxnbmJjqEmVYW7K0eWV0Jr65GKzp0wEcGS3dbv6hcwStgk1TbmhF204Yo6pJEo8g/OK+1no4EBidCcK/b12RrQOUfF3OjCbgmQA0aNoEpGeOov/rv5A59shamTq+QmaWGTbjMc9XIawfvsIoSBmboc1DT35k1NrNci3cgjCXRBEowBrmjQyp79+vVjbjM0A31fQ/ug==",
  "SigningCertURL": "https://sns.eu-west-2.amazonaws.com/SimpleNotificationService-01d088a6f77103d0fe307c0069e40ed6.pem",
  "UnsubscribeURL": "https://sns.eu-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-e29fb030a51b3576dd645aa5e460e573:42f1c169-ee8d-4967-856c-a0b28c535751",
  "MessageAttributes": {
  "eventType": {
  "Type": "String",
  "Value": "probation-case.engagement.created"
},
  "id": {
  "Type": "String",
  "Value": "97ecad55-9ca5-b88d-48dc-84b4401128ac"
},
  "timestamp": {
  "Type": "Number",
  "Value": "1697097682467"
}
}
}*/
