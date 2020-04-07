package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

@ApiModel("UnpaidWork")
@Getter
@Builder
public class UnpaidWork {
    private Integer minutesOffered;
    private Integer minutesCompleted;
    private Integer appointmentsToDate;
    private Integer attended;
    private Integer acceptableAbsences;
    private Integer unacceptableAbsences;
}
