package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

@ApiModel("UnpaidWork")
@Getter
@Builder
public class UnpaidWork {
    private Integer offered;
    private Integer completed;
    private Integer appointmentsToDate;
    private Integer attended;
    private Integer acceptableAbsences;
    private Integer unacceptableAbsences;
}
