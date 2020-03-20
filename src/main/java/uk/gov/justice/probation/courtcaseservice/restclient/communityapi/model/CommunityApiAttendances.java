package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

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
public class CommunityApiAttendances {
    @ApiModelProperty(value = "List of Attendances")
    private List<CommunityApiAttendance> attendances;

}
