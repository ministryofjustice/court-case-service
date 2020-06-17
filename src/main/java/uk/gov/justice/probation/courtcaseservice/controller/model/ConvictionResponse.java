package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

@ApiModel(description = "Conviction Response")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(Include.NON_NULL)
public class ConvictionResponse {

        @ApiModelProperty(value = "List of Attendances")
        private List<AttendanceResponse> attendances;

        @ApiModelProperty(value = "UPW")
        private UnpaidWork unpaidWork;
}
