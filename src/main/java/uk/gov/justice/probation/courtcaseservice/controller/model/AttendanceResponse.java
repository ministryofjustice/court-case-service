package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Attendance - describes a contact")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceResponse {

    @Schema(required = true)
    private final Long contactId;

    @Schema(required = true)
    private final LocalDate attendanceDate;

    @Schema(required = true)
    private final boolean attended;

    @Schema(required = true )
    private final boolean complied;

    private final String outcome;

    private final ContactTypeDetail contactType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    public static class ContactTypeDetail {

        @Schema(required = true)
        private final String description;

        @Schema(required = true)
        private final String code;

    }
}
