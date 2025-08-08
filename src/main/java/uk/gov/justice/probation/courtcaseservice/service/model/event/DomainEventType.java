package uk.gov.justice.probation.courtcaseservice.service.model.event;

import lombok.Getter;

@Getter
public enum DomainEventType {

    SENTENCED_EVENT_TYPE("court.case.sentenced"),
    NDELIUS_RECORD_LINKED_EVENT_TYPE("ndelius.record.linked"),
    NDELIUS_RECORD_UNLINKED_EVENT_TYPE("ndelius.record.unlinked");

    final String eventTypeName;

    DomainEventType(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

}
