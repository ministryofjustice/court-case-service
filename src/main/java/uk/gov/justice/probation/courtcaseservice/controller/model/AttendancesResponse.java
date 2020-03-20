package uk.gov.justice.probation.courtcaseservice.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Attendance Wrapper")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendancesResponse {

        private String crn;

        private Long convictionId;

        @ApiModelProperty(value = "List of Attendances")
        private List<AttendanceResponse> attendances;
}
