package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel("Offender Manager")
@Getter
@Builder
public class OffenderManager {
    private String forenames;
    private String surname;
    private LocalDate allocatedDate;
}
