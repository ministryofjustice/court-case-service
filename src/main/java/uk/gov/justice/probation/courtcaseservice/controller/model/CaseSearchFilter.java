package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CaseSearchFilter {
    private final String courtCode;
    private final LocalDate date;
    private final String probationStatus;
    private final String session;
    private final String courtRoom;
}
