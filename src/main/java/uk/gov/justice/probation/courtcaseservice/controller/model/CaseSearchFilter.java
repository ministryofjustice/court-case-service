package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CaseSearchFilter {
    private final String courtCode;
    private final LocalDate date;
    private final List<String> probationStatus;
    private final String session;
    private final List<String> courtRoom;
}
