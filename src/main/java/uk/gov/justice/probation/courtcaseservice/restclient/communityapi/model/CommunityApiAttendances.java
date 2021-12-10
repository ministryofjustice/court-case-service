package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Attendance Wrapper")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiAttendances {
    @Schema(description = "List of Attendances")
    private List<CommunityApiAttendance> attendances;

}
