package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@ApiModel(description = "Attendance - describes a contact")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceResponse {

    @ApiModelProperty(required = true)
    private final Long contactId;

    @ApiModelProperty(required = true)
    private final LocalDate attendanceDate;

    @ApiModelProperty(required = true)
    private final boolean attended;

    @ApiModelProperty(required = true )
    private final boolean complied;

    private final String outcome;

    private final ContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ContactTypeDetail {

        @ApiModelProperty(required = true)
        private final String description;

        @ApiModelProperty(required = true)
        private final String code;

    }
}
