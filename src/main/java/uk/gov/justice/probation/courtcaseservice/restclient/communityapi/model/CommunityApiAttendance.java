package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiAttendance {

    @ApiModelProperty(required = true)
    private Long contactId;

    @ApiModelProperty(required = true)
    private LocalDate attendanceDate;

    @ApiModelProperty(required = true)
    private boolean attended;

    @ApiModelProperty(required = true )
    private boolean complied;

    private String outcome;

    @ApiModelProperty(required = true)
    private CommunityApiContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommunityApiContactTypeDetail {

        @ApiModelProperty(required = true)
        private String description;

        @ApiModelProperty(required = true)
        private String code;

    }
}
