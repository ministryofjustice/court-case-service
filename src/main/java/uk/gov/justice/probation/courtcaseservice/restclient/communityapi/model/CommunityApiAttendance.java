package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiAttendance {

    @Schema(required = true)
    private Long contactId;

    @Schema(required = true)
    private LocalDate attendanceDate;

    @Schema(required = true)
    private boolean attended;

    @Schema(required = true )
    private boolean complied;

    private String outcome;

    @Schema(required = true)
    private CommunityApiContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommunityApiContactTypeDetail {

        @Schema(required = true)
        private String description;

        @Schema(required = true)
        private String code;

    }
}
