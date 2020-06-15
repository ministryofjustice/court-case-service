package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("UnpaidWork")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnpaidWork {
    private final Integer minutesOffered;
    private final Integer minutesCompleted;
    private final Integer appointmentsToDate;
    private final Integer attended;
    private final Integer acceptableAbsences;
    private final Integer unacceptableAbsences;
    private final String status;
}
