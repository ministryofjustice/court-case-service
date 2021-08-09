package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

public enum CourtSession {
    MORNING,
    AFTERNOON;


    public static CourtSession from(LocalDateTime sessionStartTime) {
        return sessionStartTime.getHour() < 12 ? MORNING : AFTERNOON;
    }

    public static CourtSession from(LocalTime sessionStartTime) {
        return sessionStartTime.getHour() < 12 ? MORNING : AFTERNOON;
    }
}
