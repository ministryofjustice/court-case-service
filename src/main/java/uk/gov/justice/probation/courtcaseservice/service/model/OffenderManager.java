package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OffenderManager {
    private String forenames;
    private String surname;
    private LocalDate allocatedDate;
}
