package uk.gov.justice.probation.courtcaseservice.service.model.event;

import lombok.Getter;

@Getter
public enum DomainEventType {

    SENTENCED_EVENT_TYPE("court.case.sentenced");

    final String eventTypeName;

    DomainEventType(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

}
