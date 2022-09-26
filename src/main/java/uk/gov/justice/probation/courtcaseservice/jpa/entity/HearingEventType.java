package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public enum HearingEventType {
    CONFIRMED_OR_UPDATED("ConfirmedOrUpdated"),
    RESULTED("Resulted"),
    UNKNOWN("Unknown");

    private final String name;

    HearingEventType(String hearingEventType) {
        this.name = hearingEventType;
    }

    public static HearingEventType fromString(final String hearingEventType) {

        switch (Optional.ofNullable(hearingEventType).orElse(UNKNOWN.name())) {
            case "ConfirmedOrUpdated" -> {
                return HearingEventType.CONFIRMED_OR_UPDATED;
            }
            case "Resulted" -> {
                return HearingEventType.RESULTED;
            }
            default -> {
                log.error("Received an unexpected value to map for hearing event type {}", hearingEventType);
                return HearingEventType.UNKNOWN;
            }
        }
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
