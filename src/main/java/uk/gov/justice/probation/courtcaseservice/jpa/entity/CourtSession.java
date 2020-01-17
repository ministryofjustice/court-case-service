package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDateTime;

public enum CourtSession {
    MORNING,
    AFTERNOON;


    public static CourtSession from(LocalDateTime sessionStartTime) {
        return sessionStartTime.getHour() < 12 ? MORNING : AFTERNOON;
    }
}
