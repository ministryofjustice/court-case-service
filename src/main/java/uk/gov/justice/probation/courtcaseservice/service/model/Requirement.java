package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class Requirement {
    private String rqmntTypeMainCategoryId;
    private String rqmntTypeSubCategoryId;
    private String adRqmntTypeMainCategoryId;
    private String adRqmntTypeSubCategoryId;
    private Integer length;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private String rqmntTerminationReasonId;
}
